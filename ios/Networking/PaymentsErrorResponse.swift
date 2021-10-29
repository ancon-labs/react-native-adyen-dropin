import Adyen
import AdyenNetworking

internal struct PaymentsErrorResponse: Codable, Error, LocalizedError {
        
    public let status: Int?
    
    public let errorCode: String?
    
    public let message: String?
    
    public let type: APIErrorType?
    
    public var errorDescription: String? {
        message
    }
    
    internal let resultCode: ResultCode?
    
    internal let refusalReason: String?
    
    internal let refusalReasonCode: String?
    
    internal let additionalData: AdditionalData?
    
    private enum CodingKeys: String, CodingKey {
        case resultCode
        case status
        case errorCode
        case message
        case type = "errorType"
        case refusalReason
        case refusalReasonCode
        case additionalData
    }
    
    internal init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.resultCode = try container.decodeIfPresent(ResultCode.self, forKey: .resultCode)
        self.status = try container.decodeIfPresent(Int.self, forKey: .status)
        self.errorCode = try container.decodeIfPresent(String.self, forKey: .errorCode)
        self.message = try container.decodeIfPresent(String.self, forKey: .message)
        self.type = try container.decodeIfPresent(APIErrorType.self, forKey: .type)
        self.refusalReason = try container.decodeIfPresent(String.self, forKey: .refusalReason)
        self.refusalReasonCode = try container.decodeIfPresent(String.self, forKey: .refusalReasonCode)
        self.additionalData = try container.decodeIfPresent(AdditionalData.self, forKey: .additionalData)
    }
    
    internal func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(resultCode?.rawValue, forKey: .resultCode)
        try container.encodeIfPresent(status, forKey: .status)
        try container.encodeIfPresent(errorCode, forKey: .errorCode)
        try container.encodeIfPresent(message, forKey: .message)
        try container.encodeIfPresent(type?.rawValue, forKey: .type)
        try container.encodeIfPresent(refusalReason, forKey: .refusalReason)
        try container.encodeIfPresent(refusalReasonCode, forKey: .refusalReasonCode)
        try container.encodeIfPresent(additionalData, forKey: .additionalData)
    }
}

public enum APIErrorType: String, Decodable {
    case `internal`
    case validation
    case security
    case configuration
    case urlError
    case noInternet
    case sessionExpired
}
