import { NativeModules } from 'react-native';

export type Amount = {
  value: number;
  currencyCode: string;
};

export type CardConfiguration = {
  /** @todo NOT IMPLEMENTED */
  showsHolderNameField?: boolean;
  /** @todo NOT IMPLEMENTED */
  showsStorePaymentMethodField?: boolean;
  /** @todo NOT IMPLEMENTED */
  showsSecurityCodeField?: boolean;
};

export type ApplePayConfiguration = {
  /** Label to show instead of "Total" */
  label?: string;
  amount?: Amount;
  configuration?: {
    /** Name to be displayed on the form */
    merchantName?: string;
    /** Your Apple merchant identifier */
    merchantId: string;
  };
};

export type DropInConfiguration = {
  clientKey: string;
  environment: 'test' | 'live';
  countryCode: string;
  amount: Amount;
  /** @todo NOT IMPLEMENTED */
  card?: CardConfiguration;
  applePay?: ApplePayConfiguration;
};

export type ModuleConfig = {
  /**
   * *Required* base URL ending with "/"
   * @example
   * "http://server.com/api/"
   */
  baseUrl: string;
  /**
   * Set to `true` to view more native logs
   */
  debug?: boolean;
  /** Optional custom headers to add to requests */
  headers?: {
    [key: string]: string;
  };
  /** Optional custom endpoints */
  endpoints?: {
    /**
     * Full payment URL to call, with or without "/" at beginning
     * @example
     * "/payments"
     * @example
     * "payments"
     */
    makePayment: string;
    /**
     * Full details URL to call, with or without "/" at beginning
     * @example
     * "/details"
     * @example
     * "details"
     */
    makeDetailsCall: string;
  };
};

export type PaymentMethod = {
  [key: string]: [value: string];
};

export type PaymentMethodsResponse = {
  paymentMethods?: [PaymentMethod];
};

export type PaymentResult = {
  additionalData?: any;
  message?: string;
  errorCode?: string;
  resultCode?: string;
  refusalReason?: string;
  refusalReasonCode?: string;
};

export type PaymentPromise = Promise<PaymentResult>;

export const AdyenDropInModule = NativeModules.AdyenDropInModule;

export function isCancelledError(err: unknown): boolean {
  if (err instanceof Error) {
    return err.message === 'Cancelled';
  }

  return err === 'Cancelled';
}

export function isRefusedResult(result: PaymentResult): boolean {
  return (
    result.refusalReasonCode !== undefined &&
    result.refusalReasonCode !== null &&
    result.refusalReasonCode !== '0'
  );
}

function trimStartingSlash(str?: string): string {
  if (str && str.charAt(0) === '/') {
    return str.substr(1);
  }

  return str ?? '';
}

function cleanModuleConfig(dirtyModuleConfig: ModuleConfig): ModuleConfig {
  return {
    ...dirtyModuleConfig,
    ...(dirtyModuleConfig.endpoints
      ? {
          endpoints: {
            makePayment: trimStartingSlash(
              dirtyModuleConfig.endpoints.makePayment
            ),
            makeDetailsCall: trimStartingSlash(
              dirtyModuleConfig.endpoints.makeDetailsCall
            ),
          },
        }
      : {}),
  };
}

function cleanPaymentMethodsResponse(
  dirtyPaymentMethodsResponse: PaymentMethodsResponse
): PaymentMethodsResponse {
  if (dirtyPaymentMethodsResponse.paymentMethods) {
    return dirtyPaymentMethodsResponse;
  }

  return {
    paymentMethods: dirtyPaymentMethodsResponse as PaymentMethod[],
  } as PaymentMethodsResponse;
}

const AdyenDropIn = {
  /**
   * ***Required*** Call this function with a drop-in settings before calling `start()`
   * @param dropInConfig Configuration object
   * @returns `AdyenDropIn` instance (`this`)
   */
  setDropInConfig(dropInConfig: DropInConfiguration) {
    AdyenDropInModule.setDropInConfig(dropInConfig);
    return this;
  },
  /**
   * ***Required*** Call this function to set additional settings for the RN module
   * @param moduleConfig Configuration object
   * @returns `AdyenDropIn` instance (`this`)
   */
  setModuleConfig(moduleConfig: ModuleConfig) {
    const cleanedModuleConfig = cleanModuleConfig(moduleConfig);
    AdyenDropInModule.setModuleConfig(cleanedModuleConfig);
    return this;
  },
  /**
   * Call this function to show the drop-in and start the payment flow
   * @param paymentMethodsResponse Payment methods response object
   * @returns Promise that resolves with payment result if payment finished without errors
   */
  start(paymentMethodsResponse: PaymentMethodsResponse): PaymentPromise {
    return new Promise((resolve, reject) => {
      if (!paymentMethodsResponse) {
        return reject(new Error('Missing paymentMethodsResponse argument'));
      }

      const cleanedPaymentMethodsResponse = cleanPaymentMethodsResponse(
        paymentMethodsResponse
      );

      const resolveCallback = (jsonStr: string) => {
        try {
          const parsed = JSON.parse(jsonStr);
          return resolve(parsed);
        } catch {
          return reject(
            new Error('Failed to parse JSON from native resolve callback')
          );
        }
      };

      const rejectCallback = (msg?: string) => {
        return reject(new Error(msg ?? 'Unknown error'));
      };

      try {
        AdyenDropInModule.start(
          cleanedPaymentMethodsResponse,
          resolveCallback,
          rejectCallback
        );
      } catch (err) {
        return reject(err);
      }
    });
  },
};

export default AdyenDropIn;
