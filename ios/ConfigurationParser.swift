import Adyen
import PassKit

struct ConfigurationParser {
    static func getEnvironment(_ environmentName: String) -> Environment {
        switch environmentName {
        case "test":
            return Environment.test
        case "live":
            return Environment.live
        default:
            return Environment.test
        }
    }
    
    let config: DropInComponent.Configuration
    
    init(_ apiContext: APIContext) {
        self.config = DropInComponent.Configuration(apiContext: apiContext)
    }
    
    func parse(_ config: NSDictionary) -> DropInComponent.Configuration {
        let nextConfig = self.config
        
        if let amountConfig = config.value(forKey: "amount") as? NSDictionary {
            let value = amountConfig.value(forKey: "value") as! Int
            let currencyCode = amountConfig.value(forKey: "currencyCode") as! String
            
            let amount = Amount(value: value, currencyCode: currencyCode)
            let countryCode = config.value(forKey: "countryCode") as! String
            nextConfig.payment = Payment(amount: amount, countryCode: countryCode)
        }
        
        if let applePayConfig = config.value(forKey: "applePay") as? NSDictionary {
            let amount = NSDecimalNumber(string: String(format: "%.2f", Float(nextConfig.payment?.amount.value ?? 0) / 100))
            
            let label = applePayConfig.value(forKey: "label") as? String ?? "Total"
            let summaryItem = PKPaymentSummaryItem(label: label, amount: amount, type: .final)
            
            if let amountConfig = applePayConfig.value(forKey: "amount") as? NSDictionary {
                if let applePayAmount = amountConfig.value(forKey: "value") as? Int {
                    summaryItem.amount = NSDecimalNumber(value: applePayAmount)
                }
            }
            
            if let nestedConfig = applePayConfig.value(forKey: "configuration") as? NSDictionary {
                if let merchantIdentifier = nestedConfig.value(forKey: "merchantId") as? String {
                    nextConfig.applePay = ApplePayComponent.Configuration(summaryItems: [summaryItem], merchantIdentifier: merchantIdentifier)
                }
            }
        }
        
        return nextConfig
    }
}
