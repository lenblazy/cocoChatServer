/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Lennox
 */
public class server_fr {
    
    public int port = 8888;
    
    public static AudioFormat getaudioformat(){
        float sampleRate = 8000.0F;
        int sampleSizeInbits = 16;
        int channel = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate,sampleSizeInbits,channel,signed,bigEndian);
    }
    
    public SourceDataLine audio_out;
    
    public void init_audio(){
        try {
            AudioFormat format = getaudioformat();
            DataLine.Info info_out = new DataLine.Info(SourceDataLine.class, format);
            if(!AudioSystem.isLineSupported(info_out)){
                System.out.println("Unsupported");
                System.exit(0);
            }
            audio_out = (SourceDataLine)AudioSystem.getLine(info_out);
            audio_out.open(format);
            audio_out.start();
            
            player_thread p = new player_thread();
            try { 
                p.din = new DatagramSocket(port);
                p.audio_out = audio_out;
                server_voice.calling = true;
                p.start();
                
            } catch (SocketException ex) {
                Logger.getLogger(server_fr.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (LineUnavailableException ex) {
            Logger.getLogger(server_fr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
