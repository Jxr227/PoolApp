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

public class PoolGame {
    private Deque<PoolPlayer> turns;
    private PoolPlayer player1;
    private PoolPlayer player2;

    public PoolGame(PoolPlayer player1, PoolPlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
        turns = new ArrayDeque<>();
        turns.push(player1);
        turns.push(player2);
    }

    public PoolPlayer nextTurn(boolean wasFoul) {
        PoolPlayer curr = turns.pollFirst();
        if (wasFoul) {
            turns.removeFirstOccurrence(curr);
            turns.offerFirst(oppositePlayer(curr));
        }
        turns.offerLast(curr);
        return turns.peek();
    }

    public PoolPlayer getTurn() {
        return turns.peekFirst();
    }

    /**
     *
     * @param ballsPotted
     * @param firstBallTouched
     */
    public void turn(ArrayList<BallTypes> ballsPotted, BallTypes firstBallTouched) {
        boolean hasFouled = false;
        if(firstBallTouched != getTurn().getBallType() ||
                (firstBallTouched == BallTypes.BLACK && getTurn().getScore() == 8)){
            hasFouled = true;
        }
        for (BallTypes ball : ballsPotted) {
            if (getTurn().getBallType() == null) {
                setBallTypes(getTurn(), ball);
            }
            if (getTurn().getBallType().equals(ball)) {
                getTurn().incScore();
            } else if(ball.equals(BallTypes.BLACK)) {
                if(getTurn().getScore() == 8){
                    //WINNER
                } else {
                    //LOSER
                }
            } else {
                hasFouled = true;
                if(ball.equals(oppositePlayer(getTurn()).getBallType())){
                    oppositePlayer(getTurn()).incScore();
                }
            }
        }
        nextTurn(hasFouled);
    }

    private void setBallTypes(PoolPlayer currPlayer, BallTypes pottedBall) {
        if (currPlayer.equals(player1)) {
            setBallTypes(pottedBall, pottedBall.getOpposite());
        } else if (currPlayer.equals(player2)) {
            setBallTypes(pottedBall.getOpposite(), pottedBall);
        }
    }

    private void setBallTypes(BallTypes player1BallType, BallTypes player2BallType) {
        player1.setMyBallType(player1BallType);
        player2.setMyBallType(player2BallType);
    }

    private PoolPlayer oppositePlayer(PoolPlayer player) {
        return player == player1 ? player2 : player1;
    }

    public enum BallTypes {
        YELLOWS, REDS, BLACK, WHITE;

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
