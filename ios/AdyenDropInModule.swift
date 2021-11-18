import Adyen

@objc(AdyenDropInModule)
class AdyenDropInModule: NSObject {
    internal var currentComponent: PresentableComponent?
    
    internal weak var presenter: UIViewController?
    
    private var apiClient: DefaultAPIClient?
    
    private var dropInConfiguration: DropInComponent.Configuration? = nil
    
    private var resolveCallback: RCTResponseSenderBlock? = nil
    
    private var rejectCallback: RCTResponseSenderBlock? = nil
    
    private var onSubmitCallback: RCTResponseSenderBlock? = nil
    
    private var onAdditionalDetailsCallback: RCTResponseSenderBlock? = nil
    
    // MARK: - RN module
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
      return true
    }
    
    func resolve(_ arg: Any) {
        resolveCallback?.self([arg])
    }
    
    func reject(_ arg: Any) {
        rejectCallback?.self([arg])
    }
    
    @objc
    func setDropInConfig(_ config: NSDictionary?) {
        print("Called setDropInConfig")
        
        if (config == nil) {
            reject("setDropInConfig was called without a config object")
            return
        }
        
        guard let clientKey = config?["clientKey"] as? String,
              let environmentName = config?["environment"] as? String
        else {
            reject("setDropInConfig missing clientKey or environment")
            return
        }
        
        MemoryStorage.current.clientKey = clientKey
        
        if let shopperReference = config?["shopperReference"] as? String {
            MemoryStorage.current.shopperReference = shopperReference
        }
        
        if let countryCode = config?["countryCode"] as? String {
            MemoryStorage.current.countryCode = countryCode
        }
        
        if let shopperLocale = config?["shopperLocale"] as? String {
            MemoryStorage.current.shopperLocale = shopperLocale
        }
        
        if let additionalData = config?["additionalData"] as? NSDictionary {
            if let allow3DS2 = additionalData["allow3DS2"] as? Bool {
                MemoryStorage.current.allow3DS2 = allow3DS2
            }
            if let executeThreeD = additionalData["executeThreeD"] as? Bool {
                MemoryStorage.current.executeThreeD = executeThreeD
            }
        }
        
        if let merchantAccount = config?["merchantAccount"] as? String {
            MemoryStorage.current.merchantAccount = merchantAccount
        }
        
        if let returnUrl = config?["returnUrl"] as? String {
            MemoryStorage.current.returnUrl = returnUrl
        }
        
        let environment = ConfigurationParser.getEnvironment(environmentName)
        let adyenApiContext = APIContext(environment: environment, clientKey: clientKey)
        self.dropInConfiguration = ConfigurationParser(adyenApiContext).parse(config!)
    }
    
    @objc
    func setModuleConfig(_ config: NSDictionary?) {
        print("Called setModuleConfig")
        
        guard let baseUrl = config?["baseUrl"] as? String else {
            reject("setModuleConfig missing baseUrl")
            return
        }
        
        MemoryStorage.current.baseUrl = URL(string: baseUrl)!
        
        if let debug = config?["debug"] as? Bool {
            MemoryStorage.current.debug = debug
            AdyenLogging.isEnabled = debug
        }
        
        if let headers = config?["headers"] as? [String: String] {
            MemoryStorage.current.headers = headers
        }
        
        if let endpoints = config?["endpoints"] as? NSDictionary {
            if let makePaymentEndpoint = endpoints["makePayment"] as? String {
                MemoryStorage.current.makePaymentEndpoint = makePaymentEndpoint
            }
            
            if let makeDetailsCallEndpoint = endpoints["makeDetailsCall"] as? String {
                MemoryStorage.current.makeDetailsCallEndpoint = makeDetailsCallEndpoint
            }
        }
    }
    
    @objc
    func setSubmitCallback(_ onSubmit: @escaping RCTResponseSenderBlock) {
        print("Called setSubmitCallback")
        self.onSubmitCallback = onSubmit
    }
    
    @objc
    func setAdditionalDetailsCallback(_ onAdditionalDetails: @escaping RCTResponseSenderBlock) {
        print("Called setAdditionalDetailsCallback")
        self.onAdditionalDetailsCallback = onAdditionalDetails
    }
    
    @objc
    func setPaymentResponse(_ paymentResponse: NSDictionary?) {
        print("Called setPaymentResponse")
        
        guard paymentResponse != nil else {
            return
        }

        do {
            let response = try AdyenDropInModule.decodeResponse(paymentResponse!)
            self.handleAsyncResponse(response)
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    @objc
    func setDetailsResponse(_ detailsResponse: NSDictionary?) {
        print("Called setDetailsResponse")
        
        guard detailsResponse != nil else {
            return
        }

        do {
            let response = try AdyenDropInModule.decodeResponse(detailsResponse!)
            self.handleAsyncResponse(response)
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    static func decodeResponse(_ response: NSDictionary) throws -> PaymentResponse {
        let JSON = try JSONSerialization.data(withJSONObject: response)
        let decoded = try Coder.decode(JSON) as PaymentResponse
        return decoded
    }
    
    func handleAsyncResponse(_ result: PaymentResponse) {
        switch result.resultCode {
        case .authorised, .pending, .received, .challengeShopper, .identifyShopper, .presentToShopper, .redirectShopper:
            if let action = result.action {
                handle(action)
            } else {
                DispatchQueue.main.async {
                    self.finish(with: result.resultCode)
                }
            }

        case .cancelled, .error, .refused:
            DispatchQueue.main.async {
                self.finish(with: result.resultCode)
            }
        }
    }
    
    @objc
    func start(_ paymentMethodsResponse: NSDictionary, resolveCallback: @escaping RCTResponseSenderBlock, rejectCallback: @escaping RCTResponseSenderBlock) {
        print("Called start")
        
        if (dropInConfiguration == nil) {
            reject("start was called without dropInConfig being set")
            return
        }
        
        guard let baseUrl = MemoryStorage.current.baseUrl,
              let clientKey = MemoryStorage.current.clientKey
        else {
            reject("Missing baseUrl or clientKey")
            return
        }
        
        let environment = Environment(baseURL: baseUrl)
        let apiContext = APIContext(environment: environment, clientKey: clientKey)
        self.apiClient = DefaultAPIClient(apiContext: apiContext)
        
        do {
            self.resolveCallback = resolveCallback
            self.rejectCallback = rejectCallback
            
            let jsonObject = try JSONSerialization.data(withJSONObject: paymentMethodsResponse)
            let paymentMethods = try Coder.decode(jsonObject) as PaymentMethods
            DispatchQueue.main.async {
                self.presenter = UIApplication.shared.delegate?.window??.rootViewController
                self.presentDropInComponent(paymentMethods)
            }
        } catch let err {
            reject("An error occurred while attempting to start payment: \(err.localizedDescription)")
        }
    }
    
    // MARK: - Adyen DropIn
    
    internal func presentDropInComponent(_ paymentMethods: PaymentMethods) {
        guard let dropInConfiguration = dropInConfiguration else { return }
        
        let dropInComponentStyle = DropInComponent.Style()
        let component = DropInComponent(paymentMethods: paymentMethods,
                                        configuration: dropInConfiguration,
                                        style: dropInComponentStyle,
                                        title: nil)
        component.delegate = self
        currentComponent = component
        
        presenter?.present(component.viewController, animated: true, completion: nil)
    }
    
    internal func finish(with response: PaymentsResponse) {
        let success = response.resultCode == .authorised || response.resultCode == .received || response.resultCode == .pending
        currentComponent?.finalizeIfNeeded(with: success)

        presenter?.dismiss(animated: true) { [weak self] in
            print("Dismiss successfully")
            
            do {
                let jsonObject = try JSONEncoder().encode(response)
                let str = String(data: jsonObject, encoding: .utf8)
                self?.resolve(str as Any)
            } catch let err {
                print(err.localizedDescription)
            }
        }
    }
    
    internal func finish(with resultCode: ResultCode) {
        let success = resultCode == .authorised || resultCode == .received || resultCode == .pending
        currentComponent?.finalizeIfNeeded(with: success)

        presenter?.dismiss(animated: true) { [weak self] in
            print("Dismiss successfully")
            
            do {
                let jsonObject = try JSONEncoder().encode(["resultCode": resultCode.rawValue])
                let str = String(data: jsonObject, encoding: .utf8)
                self?.resolve(str as Any)
            } catch let err {
                print(err.localizedDescription)
            }
        }
    }

    internal func finish(with error: Error) {
        currentComponent?.finalizeIfNeeded(with: false)

        presenter?.dismiss(animated: true) { [weak self] in
            if let componentError = error as? ComponentError {
                switch componentError {
                case .cancelled:
                    self?.reject("Cancelled")
                    break
                case .paymentMethodNotSupported:
                    self?.reject("Payment method not supported")
                    break
                }
            } else if let redirectComponentError = error as? RedirectComponent.Error {
                switch redirectComponentError {
                case .appNotFound:
                    self?.reject("App for payment method not found")
                    break
                }
            } else if let paymentsErrorResponse = error as? PaymentsErrorResponse {
                if (paymentsErrorResponse.resultCode == nil) {
                    // Try to create JSON object for error
                    if let jsonObject = try? JSONEncoder().encode(paymentsErrorResponse) {
                        let str = String(data: jsonObject, encoding: .utf8)
                        self?.reject(str as Any)
                    } else {
                    // If couldn't create JSON object, send just the message string
                        self?.reject((paymentsErrorResponse.message ?? paymentsErrorResponse.refusalReason ?? error.localizedDescription) as Any)
                    }
                // Finished with refused etc
                } else if let jsonObject = try? JSONEncoder().encode(paymentsErrorResponse) {
                    let str = String(data: jsonObject, encoding: .utf8)
                    self?.resolve(str as Any)
                } else {
                    self?.reject(paymentsErrorResponse.message as Any)
                }
            } else {
                self?.reject(error.localizedDescription)
            }
        }
    }
    
    // MARK: - Payment response handling
    private func paymentResponseHandler(result: Result<PaymentsResponse, Error>) {
        switch result {
        case let .success(response):
            if let action = response.action {
                handle(action)
            } else {
                finish(with: response)
            }
        case let .failure(error):
            finish(with: error)
        }
    }

    private func handle(_ action: Action) {
        (currentComponent as? DropInComponent)?.handle(action)
    }
}

extension AdyenDropInModule: DropInComponentDelegate {

    internal func didSubmit(_ data: PaymentComponentData, for paymentMethod: PaymentMethod, from component: DropInComponent) {
        if self.onSubmitCallback != nil {
            self.onSubmitCallback?([[
                "paymentMethod": data.paymentMethod.encodable.dictionary as Any,
                "storePaymentMethod": data.storePaymentMethod,
                "browserInfo": data.browserInfo as Any,
                "channel": "iOS"
            ]])
        } else {
            print("User did start: \(paymentMethod.name)")
            let headers = MemoryStorage.current.headers
            let path = MemoryStorage.current.makePaymentEndpoint
            let request = PaymentsRequest(headers: headers, path: path, data: data)
            apiClient?.perform(request, completionHandler: paymentResponseHandler)
        }
    }

    internal func didProvide(_ data: ActionComponentData, from component: DropInComponent) {
        if self.onAdditionalDetailsCallback != nil {
            self.onAdditionalDetailsCallback?([[
                "details": data.details.dictionary as Any,
                "paymentData": (data.paymentData ?? "") as String
            ]])
        } else {
            let headers = MemoryStorage.current.headers
            let path = MemoryStorage.current.makeDetailsCallEndpoint
            
            let request = PaymentDetailsRequest(
                headers: headers,
                path: path,
                details: data.details,
                paymentData: data.paymentData,
                merchantAccount: MemoryStorage.current.merchantAccount
            )
            apiClient?.perform(request, completionHandler: paymentResponseHandler)
        }
    }

    internal func didComplete(from component: DropInComponent) {
        finish(with: .authorised)
    }

    internal func didFail(with error: Error, from component: DropInComponent) {
        finish(with: error)
    }

    internal func didCancel(component: PaymentComponent, from dropInComponent: DropInComponent) {
        // Handle the event when the user closes a PresentableComponent.
        print("User did close: \(component.paymentMethod.name)")
    }

}

extension Encodable {

    var dictionary: [String: Any]? {
        guard let data = try? JSONEncoder().encode(self) else { return nil }
        return (try? JSONSerialization.jsonObject(with: data, options: .allowFragments)).flatMap { $0 as? [String: Any] }
    }

}

struct PaymentResponse: Decodable {

    internal let resultCode: ResultCode

    internal let action: Action?

    internal init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.resultCode = try container.decode(ResultCode.self, forKey: .resultCode)
        self.action = try container.decodeIfPresent(Action.self, forKey: .action)
    }

    private enum CodingKeys: String, CodingKey {
        case resultCode
        case action
    }

}
