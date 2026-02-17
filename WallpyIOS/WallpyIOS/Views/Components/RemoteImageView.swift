import SwiftUI

struct RemoteImageView: View {
    let url: URL

    var body: some View {
        AsyncImage(url: url, transaction: Transaction(animation: .easeInOut)) { phase in
            switch phase {
            case .empty:
                ZStack {
                    Color(UIColor.secondarySystemBackground)
                    ProgressView()
                }
            case .success(let image):
                image
                    .resizable()
                    .scaledToFill()
            case .failure:
                ZStack {
                    Color(UIColor.secondarySystemBackground)
                    Image(systemName: "wifi.exclamationmark")
                        .imageScale(.large)
                        .foregroundStyle(.secondary)
                }
            @unknown default:
                Color.clear
            }
        }
    }
}
