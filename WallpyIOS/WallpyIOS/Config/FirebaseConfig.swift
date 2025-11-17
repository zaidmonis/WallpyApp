import Foundation

struct FirebaseConfig: Decodable {
    let databaseURL: URL
    let categories: [String]
    let thumbnailQualitySuffix: String
    let preferredThumbnailSuffix: String
    let fullSizeQualitySuffix: String
    let versionNode: String

    static var placeholder: FirebaseConfig {
        FirebaseConfig(
            databaseURL: URL(string: "https://example.firebaseio.com")!,
            categories: ["All"],
            thumbnailQualitySuffix: "m",
            preferredThumbnailSuffix: "l",
            fullSizeQualitySuffix: "h",
            versionNode: "CurrentVersion"
        )
    }
}

struct FirebaseConfigLoader {
    private enum LoaderError: Error {
        case fileMissing
    }

    func load() throws -> FirebaseConfig {
        guard let url = Bundle.main.url(forResource: "FirebaseConfig", withExtension: "plist") else {
            throw LoaderError.fileMissing
        }

        let data = try Data(contentsOf: url)
        return try PropertyListDecoder().decode(FirebaseConfig.self, from: data)
    }
}
