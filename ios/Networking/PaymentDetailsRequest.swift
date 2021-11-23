import Adyen
import AdyenNetworking
import Foundation

internal struct PaymentDetailsRequest: APIRequest {
    
    internal typealias ResponseType = PaymentsResponse
    
    internal var path = "payments/details"
    
    internal let details: AdditionalDetails
    
    internal let paymentData: String?

    internal let merchantAccount: String?
    
    internal var counter: UInt = 0
    
    internal var method: HTTPMethod = .post
    
    internal var queryParameters: [URLQueryItem] = []
    
    internal var headers: [String: String] = [:]
    
    init(headers: [String: String]?, queryParameters: [URLQueryItem]?, path: String?, details: AdditionalDetails, paymentData: String?, merchantAccount: String?) {
        if (headers != nil) {
            self.headers = headers!
        }
        
        if (queryParameters != nil) {
            self.queryParameters = queryParameters!
        }
        
        if (path != nil) {
            self.path = path!
        }
        
        self.details = details
        self.paymentData = paymentData
        self.merchantAccount = merchantAccount
    }
    
    internal func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(details.encodable, forKey: .details)
        try container.encode(paymentData, forKey: .paymentData)
        try container.encode(merchantAccount, forKey: .merchantAccount)
    }
    
    private enum CodingKeys: String, CodingKey {
        case details
        case paymentData
        case merchantAccount
    }
}
