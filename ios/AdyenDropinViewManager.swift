import Adyen

@objc(AdyenDropinViewManager)
class AdyenDropinViewManager: RCTViewManager, RCTInvalidating {
    
  private var viewInstance: AdyenDropinView?
  
  func invalidate() {
    print("Invalidating")
    if (self.viewInstance != nil) {
      DispatchQueue.main.async {
        self.viewInstance?.invalidate()
      }
    }
  }
  
  override func view() -> (AdyenDropinView) {
    self.viewInstance = AdyenDropinView()
    return self.viewInstance!
  }
  
  override class func requiresMainQueueSetup() -> Bool {
    return true
  }
}

@objc(AdyenDropinView)
class AdyenDropinView: UIView, DropInComponentDelegate {
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
      
      if let clientKey = paymentMethodsConfiguration!.value(forKey: "clientKey") as? String {
        self._paymentMethodsConfiguration = DropInComponent.PaymentMethodsConfiguration(clientKey: clientKey)
      }
    }
  }
  
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
    
    if (self._paymentMethods != nil && self._paymentMethodsConfiguration != nil) {
      self._dropInComponent = DropInComponent(paymentMethods: _paymentMethods!, paymentMethodsConfiguration: _paymentMethodsConfiguration!)
      self._dropInComponent?.delegate = self
    } else {
      print("Skipped init because either paymentMethods or paymentMethodsConfiguration was not set")
    }

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
  
  func didSubmit(_ data: PaymentComponentData, for paymentMethod: PaymentMethod, from component: DropInComponent) {
    print("didSubmit")
  }
  
  func didProvide(_ data: ActionComponentData, from component: DropInComponent) {
    print("didProvide")
  }
  
  func didComplete(from component: DropInComponent) {
    print("didComplete")
  }
  
  func didFail(with error: Error, from component: DropInComponent) {
    print("didFail")
    self.close()
  }
  
  func didCancel(component: PaymentComponent, from dropInComponent: DropInComponent) {
    print("didCancel")
  }
}
