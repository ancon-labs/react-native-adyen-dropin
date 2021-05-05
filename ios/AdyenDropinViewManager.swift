import Adyen
import PassKit

@objc(AdyenDropInViewManager)
class AdyenDropInViewManager: RCTViewManager, RCTInvalidating {
    
  private var viewInstance: AdyenDropInView?
  
  func invalidate() {
    print("Invalidating")
    if (self.viewInstance != nil) {
      DispatchQueue.main.async {
        self.viewInstance?.invalidate()
      }
    }
  }
  
  override func view() -> (AdyenDropInView) {
    AdyenLogging.isEnabled = true
    print(AdyenLogging.isEnabled)
    self.viewInstance = AdyenDropInView()
    return self.viewInstance!
  }
  
  override class func requiresMainQueueSetup() -> Bool {
    return true
  }
}

@objc(AdyenDropInView)
class AdyenDropInView: UIView {
  private var _dropInComponent: DropInComponent?

  private var _paymentMethods: PaymentMethods?
  
  private var _paymentMethodsConfiguration: DropInComponent.PaymentMethodsConfiguration?
  
  private var _invalidating: Bool = false
  
  @objc var visible: Bool = false {
    didSet {
      if (visible) {
        self.open()
      } else {
        self.close()
      }
    }
  }
  
  @objc var paymentMethods: NSDictionary? {
    didSet {
      do {
        guard paymentMethods != nil else {
          return
        }

        let JSON = try JSONSerialization.data(withJSONObject: paymentMethods!)
        self._paymentMethods = try Coder.decode(JSON) as PaymentMethods
      } catch let err {
        print("Failed to decode paymentMethods")
        print(err)
      }
    }
  }
  
  @objc var paymentMethodsConfiguration: NSDictionary? {
    didSet {
      guard paymentMethodsConfiguration != nil else {
        return
      }
      
      if let clientKey = paymentMethodsConfiguration?.value(forKey: "clientKey") as? String {
        let parser = ConfigurationParser(clientKey)
        self._paymentMethodsConfiguration = parser.parse(paymentMethodsConfiguration!)
      }
    }
  }
  
  // Events
  @objc var onSubmit: RCTDirectEventBlock?
  @objc var onAdditionalDetails: RCTDirectEventBlock?
  @objc var onError: RCTDirectEventBlock?
  
  func isDropInVisible() -> Bool {
    return self.reactViewController()?.presentedViewController != nil && self.reactViewController()?.presentedViewController == self._dropInComponent?.viewController
  }
  
  func invalidate() {
    self._invalidating = true
    
    if (self.isDropInVisible()) {
      self._dropInComponent!.viewController.dismiss(animated: true) {
        self._dropInComponent = nil
        self._paymentMethods = nil
        self._paymentMethodsConfiguration = nil
      }
    } else {
      self._dropInComponent = nil
      self._paymentMethods = nil
      self._paymentMethodsConfiguration = nil
    }
    
    self.reactViewController()?.dismiss(animated: false, completion: nil)
  }
  
  func initDropIn() {
    if (self._paymentMethods != nil && self._paymentMethodsConfiguration != nil) {
      print("Initializing drop-in")
      self._dropInComponent = DropInComponent(paymentMethods: _paymentMethods!, paymentMethodsConfiguration: _paymentMethodsConfiguration!)
      self._dropInComponent?.delegate = self
    } else {
      print("Skipped init because either paymentMethods or paymentMethodsConfiguration was not set")
    }
  }
  
  func open() {
    print("Open was called")
    
    guard !self._invalidating else {
      print("Skipping open because invalidating")
      return
    }
    
    guard !(self.isDropInVisible()) else {
      print("Skipping open because viewController is already presenting")
      return
    }
    
    self.initDropIn()

    if (self._dropInComponent != nil) {
      self.reactViewController()?.present(self._dropInComponent!.viewController, animated: true, completion: nil)
    }
  }
  
  func close() {
    print("Close was called")
    
    guard !self._invalidating else {
      print("Skipping close because invalidating")
      return
    }
    
    guard (self.isDropInVisible()) else {
      print("Skipping close because viewController is not being presented")
      return
    }
    
    self._dropInComponent!.viewController.dismiss(animated: true) {
      self._dropInComponent = nil
      self._paymentMethods = nil
      self._paymentMethodsConfiguration = nil
    }
  }
  
  internal func finish(with resultCode: PaymentResultCode) {
    let success = resultCode == .authorised || resultCode == .received || resultCode == .pending
    print(success)
  }

  internal func finish(with error: Error) {
    let isCancelled = (error as? ComponentError) == .cancelled
    print(isCancelled)
  }
}

extension AdyenDropInView: DropInComponentDelegate {
  func didSubmit(_ data: PaymentComponentData, for paymentMethod: PaymentMethod, from component: DropInComponent) {
    self.onSubmit?([
      "paymentMethod": data.paymentMethod.encodable.dictionary as Any,
      "storePaymentMethod": data.storePaymentMethod,
      "browserInfo": data.browserInfo as Any,
      "channel": "iOS"
    ])
  }
  
  func didProvide(_ data: ActionComponentData, from component: DropInComponent) {
    print("didProvide")
    self.onAdditionalDetails?([
      "details": data.details.dictionary as Any,
      "paymentData": data.paymentData?.dictionary as Any
    ])
  }
  
  func didComplete(from component: DropInComponent) {
    print("didComplete")
  }
  
  func didFail(with error: Error, from component: DropInComponent) {
    print("didFail")
    self.onError?(["error": error.localizedDescription])
    self.close()
  }
  
  func didCancel(component: PaymentComponent, from dropInComponent: DropInComponent) {
    print("didCancel")
  }
}

extension Encodable {
  var dictionary: [String: Any]? {
    guard let data = try? JSONEncoder().encode(self) else { return nil }
    return (try? JSONSerialization.jsonObject(with: data, options: .allowFragments)).flatMap { $0 as? [String: Any] }
  }
}
