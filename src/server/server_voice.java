package server;

public class server_voice {
    
    public static boolean calling= false;
    
    public static void main(String[] args){
        server_fr fr = new server_fr();
        fr.init_audio();
    }
    
}
