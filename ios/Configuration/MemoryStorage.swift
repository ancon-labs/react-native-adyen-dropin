import Adyen
class MemoryStorage {
    static let current = MemoryStorage()
    
    private init() {}
    
    // MARK: - RN module related
    var baseUrl: URL? = nil
    
    var debug: Bool = false
    
    var headers: [String: String]? = nil
    
    var makePaymentEndpoint: String = "payments"
    
    var makeDetailsCallEndpoint: String = "payments/details"
    
    // MARK: - Adyen DropIn related
    
    var clientKey: String? = nil
    
    var shopperReference: String? = nil
    
    var countryCode: String? = nil
    
    var shopperLocale: String = Locale.current.identifier
    
    var allow3DS2: Bool = true
    
    var executeThreeD: Bool = true
    
    var returnUrl: String?

    var merchantAccount: String?
    
    func getAdditionalData() -> [String: Bool] {
        return ["allow3DS2": allow3DS2, "executeThreeD": executeThreeD]
    }
}
