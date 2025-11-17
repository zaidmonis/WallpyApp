import SwiftUI

struct WallpaperGridView: View {
    let wallpapers: [Wallpaper]
    let hdThumbnailsEnabled: Bool
    let onSelect: (Wallpaper) -> Void

    private let gridLayout = [
        GridItem(.adaptive(minimum: 120), spacing: 12)
    ]

    var body: some View {
        ScrollView {
            LazyVGrid(columns: gridLayout, spacing: 12) {
                ForEach(wallpapers) { wallpaper in
                    Button {
                        onSelect(wallpaper)
                    } label: {
                        WallpaperCard(wallpaper: wallpaper, hdThumbnailsEnabled: hdThumbnailsEnabled)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.bottom)
        }
    }
}
