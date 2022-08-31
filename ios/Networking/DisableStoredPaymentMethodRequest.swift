import Adyen
import AdyenNetworking
import Foundation

internal struct DisableStoredPaymentMethodRequest: Request {
    
    typealias ResponseType = DisableStoredPaymentMethodResponse
    
    typealias ErrorResponseType = PaymentsErrorResponse
    
    internal var counter: UInt = 0
    
    internal var path = "disable"
    
    internal let recurringDetailReference: String
    
    internal var method: HTTPMethod = .post

    internal var headers: [String: String] = MemoryStorage.current.headers ?? [:]

    internal var queryParameters: [URLQueryItem] = MemoryStorage.current.queryParameters ?? []
    
    internal func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        try container.encode(recurringDetailReference, forKey: .recurringDetailReference)
        try container.encode(MemoryStorage.current.shopperReference, forKey: .shopperReference)
        try container.encode(MemoryStorage.current.merchantAccount, forKey: .merchantAccount)
    }

    private enum CodingKeys: String, CodingKey {
        case recurringDetailReference
        case shopperReference
        case merchantAccount
    }

}

internal struct DisableStoredPaymentMethodResponse: Response {

    internal enum ResultCode: String, Decodable {
        case detailsDisabled = "[detail-successfully-disabled]"
        case allDetailsDisabled = "[all-details-successfully-disabled]"
    }

    internal let response: ResultCode
}
