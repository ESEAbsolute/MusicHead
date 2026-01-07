package tcc.youajing.musichead.objects;

public class PlayerSettings {
    private int volume;
    private boolean musicEnabled;

    public PlayerSettings(int volume, boolean musicEnabled) {
        this.volume = volume;
        this.musicEnabled = musicEnabled;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }
}