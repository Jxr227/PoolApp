package pool;/*
* Copyright 2017 HM Revenue & Customs
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.util.*;

import static pool.PoolGame.BallTypes.REDS;
import static pool.PoolGame.BallTypes.YELLOWS;

public class PoolGame {
    private Deque<PoolPlayer> turns;
    private PoolPlayer player1;
    private PoolPlayer player2;

    public PoolGame(PoolPlayer player1, PoolPlayer player2) {
        turns.push(player1);
        turns.push(player2);
    }

    public PoolPlayer nextTurn(){
        PoolPlayer curr = turns.pollFirst();
        turns.offerLast(curr);
        return turns.peek();
    }

    public PoolPlayer getTurn(){
        return turns.peekFirst();
    }

    public void turn(ArrayList<BallTypes> ballsPotted){
        for(BallTypes ball: ballsPotted){
            if(getTurn().getBallType() == null){

            }
        }

    }

    private void calcBallTypes(PoolPlayer currPlayer, BallTypes pottedBall){
        BallTypes opponentsBall = pottedBall.getOpposite();
        if(currPlayer.equals(player1)){
            setBallTypes(pottedBall, pottedBall.getOpposite());
        } else {
            setBallTypes(pottedBall.getOpposite(), pottedBall);
        }
    }

    private void setBallTypes(BallTypes player1BallType, BallTypes player2BallType){
        player1.setMyBallType(player1BallType);
        player2.setMyBallType(player2BallType);
    }

    private void foul(){
        PoolPlayer curr = turns.pollFirst();
        /*
            In case the player had an extra turn before fouling - This player should never have
            more than the head followed by one more move
         */
        turns.removeFirstOccurrence(curr);
        turns.offerLast(curr);
    }

    public enum BallTypes {
        YELLOWS, REDS;

        private BallTypes opposite;

        static {
            YELLOWS.opposite = REDS;
            REDS.opposite = YELLOWS;
        }

        public BallTypes getOpposite() {
            return opposite;
        }

    }

}
