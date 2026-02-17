import Foundation

struct Wallpaper: Identifiable, Hashable {
    let id: String
    let originalURL: URL
    let thumbnailURL: URL
    let fullSizeURL: URL

    init?(urlString: String, transformer: ImgurURLTransformer) {
        guard let originalURL = URL(string: urlString) else { return nil }
        self.id = originalURL.absoluteString
        self.originalURL = originalURL
        self.thumbnailURL = transformer.thumbnailURL(for: originalURL)
        self.fullSizeURL = transformer.fullResolutionURL(for: originalURL)
    }
}
