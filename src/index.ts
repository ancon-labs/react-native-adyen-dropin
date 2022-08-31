import { NativeModules } from 'react-native';

export type Amount = {
  value: number;
  currencyCode: string;
};

export type CardConfiguration = {
  showsHolderNameField: boolean;
  showsStorePaymentMethodField: boolean;
  showsSecurityCodeField: boolean;
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
  merchantAccount?: string;
  card?: CardConfiguration;
  applePay?: ApplePayConfiguration;
  returnUrl?: string;
  shopperReference?: string;
  showRemovePaymentMethodButton?: boolean;
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
  /** Optional Set to `true` to disable native requests.
   *
   * Remember to provide custom callbacks and call setPaymentResponse
   * and setDetailsResponse.
   *
   * **Unsafe! The JavaScript thread might get paused on Android!**
   */
  disableNativeRequests?: boolean;
  /** Optional custom headers to add to requests */
  headers?: {
    [key: string]: string;
  };
  /** Optional custom query parameters to add to requests */
  queryParameters?: {
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
    makePayment?: string;
    /**
     * Full details URL to call, with or without "/" at beginning
     * @example
     * "/details"
     * @example
     * "details"
     */
    makeDetailsCall?: string;
    /**
     * Optional URL to call, with or without "/" at beginning
     * @example
     * "/disable"
     * @example
     * "disable"
     */
    disableStoredPaymentMethod?: string;
  };
  /** Optional custom callbacks */
  callbacks?: {
    onSubmit: (data: any) => void;
    onAdditionalDetails: (data: any) => void;
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

export enum RESULT_CODE {
  cancelled = 'cancelled',
  refused = 'refused',
  error = 'error',
  received = 'received',
}

export const AdyenDropInModule = NativeModules.AdyenDropInModule;

export function isCancelledError(err: unknown): boolean {
  if (err instanceof Error) {
    return err.message === 'Cancelled';
  }

  return err === 'Cancelled';
}

/**
 * Check if a payment result is considered successful
 * @param result Resolved payment promise result
 * @returns Whether or not the result is considered successful
 */
export function isSuccessResult(result: PaymentResult): boolean {
  if (typeof result.resultCode === 'string') {
    switch (result.resultCode.toLowerCase?.()) {
      case RESULT_CODE.cancelled:
      case RESULT_CODE.refused:
      case RESULT_CODE.error:
        return false;

      default:
        return true;
    }
  }

  if (result.errorCode) return false;

  return false;
}

function trimStartingSlash(str?: string): string | undefined {
  if (!str) return undefined;

  if (str && str.charAt(0) === '/') {
    return str.substring(1);
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
            disableStoredPaymentMethod: trimStartingSlash(
              dirtyModuleConfig.endpoints.disableStoredPaymentMethod
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
    if (moduleConfig.callbacks) {
      AdyenDropInModule.setSubmitCallback(moduleConfig.callbacks.onSubmit);
      AdyenDropInModule.setAdditionalDetailsCallback(
        moduleConfig.callbacks.onAdditionalDetails
      );
    }
    return this;
  },
  /**
   * ***Optional*** Call this function to set payment response for the RN module
   * @param paymentResponse Payment response object
   * @returns `AdyenDropIn` instance (`this`)
   */
  setPaymentResponse(paymentResponse: any) {
    AdyenDropInModule.setPaymentResponse(paymentResponse);
    return this;
  },
  /**
   * ***Optional*** Call this function to set details response for the RN module
   * @param detailsResponse Details response object
   * @returns `AdyenDropIn` instance (`this`)
   */
  setDetailsResponse(detailsResponse: any) {
    AdyenDropInModule.setDetailsResponse(detailsResponse);
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

      const rejectCallback = (msgOrJsonStr?: string) => {
        if (msgOrJsonStr) {
          try {
            const parsed = JSON.parse(msgOrJsonStr);
            const parsedEntries = Object.entries(parsed);
            const hasOnlyMessage =
              parsedEntries.length === 1 &&
              typeof parsed.message === 'string' &&
              parsed.message.length > 0;

            if (hasOnlyMessage) {
              return reject(new Error(parsed.message));
            } else {
              const { message } = parsed;
              const error = new Error(
                message ?? parsed.refusalReason ?? 'Unknown error'
              );
              parsedEntries.forEach(([key, value]) => {
                Object.defineProperty(error, key, { value });
              });
              return reject(error);
            }
          } catch {}
        }
        return reject(new Error(msgOrJsonStr ?? 'Unknown error'));
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
