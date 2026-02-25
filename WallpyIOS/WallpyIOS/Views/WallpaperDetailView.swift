import SwiftUI

struct WallpaperDetailView: View {
    let wallpaper: Wallpaper
    let useHDPreview: Bool
    let photoLibraryService: PhotoLibraryService
    @State private var isSaving = false
    @State private var saveError: String?
    @State private var isShowingError = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack {
                Spacer()
                RemoteImageView(url: useHDPreview ? wallpaper.fullSizeURL : wallpaper.originalURL)
                    .aspectRatio(contentMode: .fit)
                    .cornerRadius(24)
                    .padding()
                Spacer()
                actions
            }
            .navigationTitle("Preview")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
            .alert("Unable to save", isPresented: $isShowingError, actions: {
                Button("OK", role: .cancel) {
                    isShowingError = false
                }
            }, message: {
                Text(saveError ?? "Unknown error")
            })
        }
    }

    private var actions: some View {
        VStack(spacing: 12) {
            Button {
                Task { await saveToPhotos() }
            } label: {
                Label("Save to Photos", systemImage: "square.and.arrow.down")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(isSaving)

            ShareLink(item: wallpaper.fullSizeURL) {
                Label("Share", systemImage: "square.and.arrow.up")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
        .padding([.horizontal, .bottom])
    }

    private func saveToPhotos() async {
        guard !isSaving else { return }
        isSaving = true
        defer { isSaving = false }
        do {
            let (data, _) = try await URLSession.shared.data(from: wallpaper.fullSizeURL)
            try await photoLibraryService.saveImage(data: data)
        } catch {
            saveError = error.localizedDescription
            isShowingError = true
        }
    }
}
