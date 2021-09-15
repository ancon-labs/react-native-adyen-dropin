interface CardConfiguration {
  showsHolderNameField: boolean;
  showsStorePaymentMethodField: boolean;
  showsSecurityCodeField: boolean;
}

interface Amount {
  value: number;
  currencyCode: string;
}

interface ApplePayConfiguration {
  amount?: {
    value: number;
    currency: string;
  };
  countryCode?: string;
  configuration?: {
    merchantName?: string; // Name to be displayed on the form
    merchantId: string; // Your Apple merchant identifier
  };
}

interface ConfigInterface {
  clientKey: string;
  environment: 'test' | 'live';
  countryCode: string;
  payment?: Amount;
  card?: CardConfiguration;
  applePay?: ApplePayConfiguration;
}

const config = <ConfigInterface>{
  clientKey: '<enter clientKey here>',
  environment: 'test',
  countryCode: '<enter countryCode here>',
  applePay: {
    label: '<enter company name here>',
    configuration: {
      merchantId: '<enter merchantId here>',
    },
  },
};

export default config;
