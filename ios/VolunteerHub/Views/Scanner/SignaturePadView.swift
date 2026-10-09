import SwiftUI
import UIKit
import PencilKit

struct SignaturePadView: UIViewRepresentable {
    @Binding var canvasView: PKCanvasView
    
    func makeUIView(context: Context) -> PKCanvasView {
        canvasView.drawingPolicy = .anyInput // Allows finger drawing, not just Apple Pencil
        canvasView.tool = PKInkingTool(.pen, color: .black, width: 4)
        canvasView.backgroundColor = .clear
        canvasView.isOpaque = false
        return canvasView
    }
    
    func updateUIView(_ uiView: PKCanvasView, context: Context) {
        // No dynamic updates needed for now
    }
}

extension PKCanvasView {
    /// Renders the current drawing into a PNG representation.
    func renderToPNG() -> Data? {
        let rect = self.bounds
        let traitCollection = UITraitCollection(userInterfaceStyle: .light)
        let image = self.drawing.image(from: rect, scale: 2.0)
        return image.pngData()
    }
    
    /// Clears the canvas.
    func clear() {
        self.drawing = PKDrawing()
    }
}
