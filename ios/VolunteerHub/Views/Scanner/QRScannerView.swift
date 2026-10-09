import SwiftUI
import AVFoundation

struct QRScannerView: UIViewControllerRepresentable {
    @Binding var isScanning: Bool
    let onResult: (Result<String, Error>) -> Void

    func makeUIViewController(context: Context) -> QRScannerViewController {
        let controller = QRScannerViewController()
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: QRScannerViewController, context: Context) {
        if isScanning && !uiViewController.isSessionRunning {
            uiViewController.startScanning()
        } else if !isScanning && uiViewController.isSessionRunning {
            uiViewController.stopScanning()
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onResult: onResult)
    }

    class Coordinator: NSObject, QRScannerViewControllerDelegate {
        let onResult: (Result<String, Error>) -> Void

        init(onResult: @escaping (Result<String, Error>) -> Void) {
            self.onResult = onResult
        }

        func qrScanner(_ controller: QRScannerViewController, didScanCode code: String) {
            // Provide haptic feedback on successful scan
            UINotificationFeedbackGenerator().notificationOccurred(.success)
            onResult(.success(code))
        }

        func qrScanner(_ controller: QRScannerViewController, didFailWithError error: Error) {
            onResult(.failure(error))
        }
    }
}

protocol QRScannerViewControllerDelegate: AnyObject {
    func qrScanner(_ controller: QRScannerViewController, didScanCode code: String)
    func qrScanner(_ controller: QRScannerViewController, didFailWithError error: Error)
}

final class QRScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    weak var delegate: QRScannerViewControllerDelegate?
    private var captureSession: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!

    var isSessionRunning: Bool {
        captureSession?.isRunning == true
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.black
        captureSession = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            delegate?.qrScanner(self, didFailWithError: NSError(domain: "QRScanner", code: 1, userInfo: [NSLocalizedDescriptionKey: "No video device found."]))
            return
        }
        
        let videoInput: AVCaptureDeviceInput
        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            delegate?.qrScanner(self, didFailWithError: error)
            return
        }

        if captureSession.canAddInput(videoInput) {
            captureSession.addInput(videoInput)
        } else {
            delegate?.qrScanner(self, didFailWithError: NSError(domain: "QRScanner", code: 2, userInfo: [NSLocalizedDescriptionKey: "Could not add video input."]))
            return
        }

        let metadataOutput = AVCaptureMetadataOutput()
        if captureSession.canAddOutput(metadataOutput) {
            captureSession.addOutput(metadataOutput)
            
            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        } else {
            delegate?.qrScanner(self, didFailWithError: NSError(domain: "QRScanner", code: 3, userInfo: [NSLocalizedDescriptionKey: "Could not add metadata output."]))
            return
        }

        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        
        // Add a focus square overlay
        addOverlay()
    }

    func startScanning() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.captureSession?.startRunning()
        }
    }

    func stopScanning() {
        captureSession?.stopRunning()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        startScanning()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopScanning()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        previewLayer?.frame = view.layer.bounds
    }

    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let stringValue = readableObject.stringValue else { return }
            
            // Stop scanning immediately to prevent multiple scans
            stopScanning()
            delegate?.qrScanner(self, didScanCode: stringValue)
        }
    }
    
    private func addOverlay() {
        let size: CGFloat = 250
        let overlayView = UIView()
        overlayView.layer.borderColor = UIColor.systemGreen.cgColor
        overlayView.layer.borderWidth = 3
        overlayView.layer.cornerRadius = 12
        overlayView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(overlayView)
        
        NSLayoutConstraint.activate([
            overlayView.widthAnchor.constraint(equalToConstant: size),
            overlayView.heightAnchor.constraint(equalToConstant: size),
            overlayView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            overlayView.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }
}
