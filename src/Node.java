import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Saves serveral infos about the nodes the KiAlgorithmus runs through. Helps to test and debug the Algorithm
 */

public class Node {
    private int alphaStart;
    private int alphaEnd;
    private int betaStart;
    private int betaEnd;
    private int maxValueStart;
    private int moveNumber;
    private boolean maximizer;
    public int maxValueEnd;
    private ArrayList<Integer> alphas=new ArrayList<>();
    private ArrayList<Integer> betas=new ArrayList<>();
    private ArrayList<Integer> maxValues=new ArrayList<>();
    private ArrayList<Node>followingNodes=new ArrayList<>();
    public ArrayList <Integer> endNodes=new ArrayList<>();

    Node(int maxValue){
        this.maxValues.add(maxValue);
    }

    Node(int maxValue,char type, int valueAlphaBeta){
        switch (type){
            case 'a':
                this.alphas.add(valueAlphaBeta);
                break;
            case 'b':
                this.betas.add(valueAlphaBeta);
                break;
            default:
                if(GameInfo.notTestMode)System.out.println("Not a valid type for AlphaBeta");
                exit(-1);
        }
    }

    Node(int maxValue,int alpha, int beta, boolean maximizer){
        this.maxValueStart=maxValue;
        this.alphaStart=alpha;
        this.betaStart=beta;
        this.maximizer=maximizer;
    }

    public void addMaxAndAlpha(int alpha, int i){
        this.maxValueEnd=alpha;
        this.alphas.add(alpha);
        this.alphaEnd=alpha;
        this.moveNumber=i;
    }
    public void addMaxAndBeta(int beta, int i){
        this.maxValueEnd=beta;
        this.betas.add(beta);
        this.betaEnd=beta;
        this.moveNumber=i;
    }
    public void addMax(int max){
        this.maxValueEnd=max;
    }
    public void addEndNodeValue(int endNodeValue){
        this.endNodes.add(endNodeValue);
    }

    public void addNode(Node node){
        this.followingNodes.add(node);
    }

    /*public void outputRekursiv(){

        if(followingNodes.size()!=0){
            for(Node node: followingNodes){
                node.output();
            }
            if(GameInfo.notTestMode)System.out.println();

            for (Node node: followingNodes){
                node.outputRekursiv();
            }
        }

    }

    public void output(){
        System.out.print(this.maxValueEnd);
    }*/
}
