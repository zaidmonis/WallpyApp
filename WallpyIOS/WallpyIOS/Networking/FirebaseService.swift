import Foundation

struct FirebaseService {
    private let config: FirebaseConfig
    private let session: URLSession

    init(config: FirebaseConfig, session: URLSession = .shared) {
        self.config = config
        self.session = session
    }

    func fetchWallpapers(category: String, transformer: ImgurURLTransformer) async throws -> [Wallpaper] {
        let url = config.databaseURL.appendingPathComponent("\(category).json")
        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse, 200..<300 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }
        let urlStrings = try decodeURLStrings(from: data)
        return urlStrings.compactMap { Wallpaper(urlString: $0, transformer: transformer) }
    }

    func fetchRemoteAppVersion() async throws -> Int {
        let url = config.databaseURL.appendingPathComponent("\(config.versionNode).json")
        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse, 200..<300 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }
        if let values = try? JSONDecoder().decode([Int].self, from: data), let version = values.first {
            return version
        }
        if let value = try? JSONDecoder().decode(Int.self, from: data) {
            return value
        }
        if let dict = try? JSONDecoder().decode([String: Int].self, from: data), let version = dict.values.sorted().first {
            return version
        }
        throw URLError(.cannotParseResponse)
    }

    private func decodeURLStrings(from data: Data) throws -> [String] {
        if let array = try? JSONDecoder().decode([String].self, from: data) {
            return array
        }
        if let dict = try? JSONDecoder().decode([String: String].self, from: data) {
            return dict
                .sorted { lhs, rhs in lhs.key < rhs.key }
                .map { $0.value }
        }
        if let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] {
            return object.values.compactMap { $0 as? String }
        }
        throw URLError(.cannotParseResponse)
    }
}
