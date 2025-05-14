package max.iv.usersubscriptionservice.models.enums;

public enum ServiceName {
    YOUTUBE_PREMIUM("YouTube Premium"),
    VK_MUSIC("VK Музыка"),
    YANDEX_PLUS("Яндекс.Плюс"),
    NETFLIX_STANDARD("Netflix Standard"),
    SPOTIFY_PREMIUM("Spotify Premium"),
    APPLE_MUSIC("Apple Music");

    private final String displayName;

    ServiceName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
