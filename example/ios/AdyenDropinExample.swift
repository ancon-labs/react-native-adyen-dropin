import Foundation
import Adyen

@objc class AdyenObjectiveCBridge: NSObject {

  @objc(applicationDidOpenURL:)
  static func applicationDidOpen(_ url: URL) -> Bool {
     let adyenHandled = RedirectComponent.applicationDidOpen(from : url)
     return adyenHandled
  }
}
