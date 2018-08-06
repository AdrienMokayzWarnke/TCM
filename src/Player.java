import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static final HashMap<Integer, Card> CARDSET = new HashMap<>();

    public static void main(String args[]) {


        double[][] refValue = new double[160][4];

        System.out.println(refValue.length);

        loadCards("cardlist.txt");

        double attack = 0.8;
        double breakthrough = 0.16116106;
        double charge = 0.25;
        double drain = 0.4656842;
        double drawCards = 1.510526458;
        double guard = 0.2301600155;
        double heal = 0.3327815325;
        double health = 0.468508454;
        double lethal = 0.125;//0.6501600155
        double ward = 0.368508454;


        int ref = 0;
        for (Map.Entry<Integer, Card> entry : CARDSET.entrySet()) {
            Card card = entry.getValue();
            double value = 0;
            if (card.cost < 3) {
                value = (card.attack * attack + card.defense * health + 1) / 2;
            } else {
                value = (card.attack * attack + card.defense * health+1)/1.6;
            }
                if (card.keywords.hasCharge) {
                    value += charge * card.attack;
                }
                if (card.keywords.hasDrain) {
                    value += drain;
                }
                if (card.keywords.hasLethal) {
                    value += 0.4 + lethal * card.defense;
                }
                if (card.keywords.hasWard) {
                    value += ward * card.attack;
                }
                if (card.keywords.hasBreakthrough) {
                    value += breakthrough * card.attack;
                }
                if (card.keywords.hasGuard) {
                    value += guard * card.defense;
                }
                if(card.cardDraw>0){
                    value+= drawCards * card.cardDraw;
                }
                if(card.oppHealthChange>0){
                    value += 1/30 * card.oppHealthChange;
                }
                if(card.myHealthChange>0){
                    value += 1/30 * card.myHealthChange;
                }
                if (!card.type.equals(Card.Type.CREATURE)) {
                    System.out.println("id :" + card.baseId);
                    System.out.println("real value :" + card.cost);
                    System.out.println("estimated value :" + value);
                }
                value /= card.cost;
                refValue[0][0] = value;
                ref++;
            }


            int[][] idealCurveForMana = new int[12][12];

        for(int i = 0; i<3;i++){
            idealCurveForMana[i][0]=4;
            idealCurveForMana[i][1]=8;
            idealCurveForMana[i][2]=12;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=0;
            idealCurveForMana[i][5]=0;
            idealCurveForMana[i][6]=0;
            idealCurveForMana[i][7]=0;
            idealCurveForMana[i][8]=0;
            idealCurveForMana[i][9]=0;
            idealCurveForMana[i][10]=0;
            idealCurveForMana[i][11]=0;
        }

        for(int i = 3; i<6;i++){
            idealCurveForMana[i][0]=0;
            idealCurveForMana[i][1]=4;
            idealCurveForMana[i][2]=8;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=4;
            idealCurveForMana[i][5]=4;
            idealCurveForMana[i][6]=2;
            idealCurveForMana[i][7]=1;
            idealCurveForMana[i][8]=1;
            idealCurveForMana[i][9]=0;
            idealCurveForMana[i][10]=0;
            idealCurveForMana[i][11]=0;
        }

        for(int i = 6; i<12;i++){
            idealCurveForMana[i][0]=0;
            idealCurveForMana[i][1]=0;
            idealCurveForMana[i][2]=6;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=4;
            idealCurveForMana[i][5]=3;
            idealCurveForMana[i][6]=2;
            idealCurveForMana[i][7]=2;
            idealCurveForMana[i][8]=2;
            idealCurveForMana[i][9]=3;
            idealCurveForMana[i][10]=1;
            idealCurveForMana[i][11]=1;
        }


        for(int i = 0; i <idealCurveForMana.length;i++){
            if(i>0){
                System.out.println("");
            }
            System.out.println("*****" + i + "*****");
            for(int j = 0; j <idealCurveForMana[0].length;j++){
                System.out.print(" " +idealCurveForMana[i][j]);
            }
        }
    }


    public static final void loadCards (String cardpath){
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(cardpath), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("//.*", "").trim();
                if (line.length() > 0) {
                    Card c = new Card(line.split("\\s*;\\s*"));
                    CARDSET.put(c.baseId, c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

