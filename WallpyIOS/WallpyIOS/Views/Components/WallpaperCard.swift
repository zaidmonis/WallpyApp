import SwiftUI

struct WallpaperCard: View {
    let wallpaper: Wallpaper
    let hdThumbnailsEnabled: Bool

    var body: some View {
        RemoteImageView(url: hdThumbnailsEnabled ? wallpaper.fullSizeURL : wallpaper.thumbnailURL)
            .aspectRatio(9.0 / 16.0, contentMode: .fill)
            .frame(maxWidth: .infinity)
            .clipped()
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.white.opacity(0.08), lineWidth: 1)
            )
    }
}
