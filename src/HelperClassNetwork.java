public class HelperClassNetwork {
    public static void main(String [] args){
        String ip = "127.0.0.1";
        int port = 7777;

        for ( int i = 0; i < args.length; i++ ) {
            if ( args[i].equals("-i") ) {
                i++;
                ip = args[i];
            } else if ( args[i].equals("-p") ) {
                i++;
                port = Integer.parseInt(args[i]);
            } else if ( args[i].equals("-n") ) {
                GameInfo.sortMoves = false;
            } else if( args[i].equals("-l") ){
                GameInfo.loggMode =true;
            }else if( args[i].equals("-a") ){
                GameInfo.aplphaBetaPruning = false;
            }else if( args[i].equals("-w") ){
                GameInfo.aspirationWindow = true;
            }
            else if ( args[i].equals("-d") ) {
                GameInfo.iterativeDeepening = true;
            }

            else if ( args[i].equals("-t") ) {
                GameInfo.notTestMode = false;
            } else if( args[i].equals("-s") ||args[i].equals("-q")){
                GameInfo.notTestMode=false;
                GameInfo.loggMode=false;
                GameInfo.silentMode=true;
                GameInfo.quietMode = true;
            } else if ( args[i].equals("-h") ) {
                System.out.println("-i <ip>     Set ip to connect to");
                System.out.println("-p <port>   Set port to connect to");
                System.out.println("-n          Disable sorted moves");
                System.out.println("-q          Set flag to suppress all console outputs");
                System.out.println("-t          Enable test mode");
                System.out.println("-l          Enable log mode");
                System.out.println("-a          Disable alphaBetaPruning");
                System.out.println("-w          Enable aspirationWindows");
                System.out.println("-d          Enable IterativeDeepeningWithoutTimelimit");
                System.out.println("-s          Enable silentMode");
                System.out.println("-h          Show help");

            }

        }
        if(GameInfo.notTestMode)System.out.println("Connecting with ip: " + ip + " and port: " + port);
        Client connector = new Client(ip, port);
        connector.initConnection();
    }
}