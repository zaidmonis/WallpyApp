import Foundation

struct ImgurURLTransformer {
    private let thumbnailSuffix: String
    private let preferredThumbnailSuffix: String
    private let fullSizeSuffix: String
    private let supportedExtensions = ["jpg", "jpeg", "png"]

    init(config: FirebaseConfig) {
        thumbnailSuffix = config.thumbnailQualitySuffix
        preferredThumbnailSuffix = config.preferredThumbnailSuffix
        fullSizeSuffix = config.fullSizeQualitySuffix
    }

    func thumbnailURL(for original: URL) -> URL {
        transform(original, replacing: thumbnailSuffix, with: preferredThumbnailSuffix)
    }

    func fullResolutionURL(for original: URL) -> URL {
        transform(original, replacing: thumbnailSuffix, with: fullSizeSuffix)
    }

    private func transform(_ original: URL, replacing oldSuffix: String, with newSuffix: String) -> URL {
        var absolute = original.absoluteString
        for ext in supportedExtensions {
            let oldValue = "\(oldSuffix).\(ext)"
            let newValue = "\(newSuffix).\(ext)"
            if absolute.contains(oldValue) {
                absolute = absolute.replacingOccurrences(of: oldValue, with: newValue)
            }
        }
        return URL(string: absolute) ?? original
    }
}
