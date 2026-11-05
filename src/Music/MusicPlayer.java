package Music;
import javax.sound.sampled.*;
import java.io.File;

public class MusicPlayer {

    private Clip clip;

    // Start background music
    public void play() {
        try {
            File musicFile = new File("MusicLibrary/The%20Men%20-%20New%20Song.wav");

            AudioInputStream audioStream =
                AudioSystem.getAudioInputStream(musicFile);

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            audioStream.close();

            // Play music continuously
            clip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            System.out.println("Could not play music: " + e.getMessage());
        }
    }

    // Stop background music
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    // Adjust music volume (0.0 to 1.0)
    public void setVolume(float volume) {
        if (clip != null &&
            clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {

            FloatControl gain =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (volume <= 0) {
                gain.setValue(gain.getMinimum());
            } else {
                volume = Math.min(1.0f, volume);

                float dB = (float) (20 * Math.log10(volume));

                gain.setValue(Math.max(
                    gain.getMinimum(),
                    Math.min(gain.getMaximum(), dB)
                ));
            }
        }
    }
}