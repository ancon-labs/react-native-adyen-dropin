import Adyen
import AdyenNetworking
import Foundation

internal final class DefaultAPIClient: AnyRetryAPIClient {
    
    internal let apiClient: RetryAPIClient
    
    init(apiContext: APIContext) {
        self.apiClient = RetryAPIClient(apiClient: APIClient(apiContext: apiContext), scheduler: SimpleScheduler(maximumCount: 2))
    }
    
    internal func perform<R>(_ request: R, completionHandler: @escaping (Result<R.ResponseType, Error>) -> Void) where R: Request {
        perform(request, shouldRetry: nil, completionHandler: completionHandler)
    }
    
    internal func perform<R>(
        _ request: R, shouldRetry: ((Result<R.ResponseType, Error>) -> Bool)?,
        completionHandler: @escaping (Result<R.ResponseType, Error>) -> Void
    ) where R: Request {
        apiClient.perform(request, shouldRetry: {
            if case .failure = $0 {
                return true
            }
            return false
        }, completionHandler: completionHandler)
    }
    
    
}
