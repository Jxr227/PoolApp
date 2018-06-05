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


public class PoolPlayer {
    private String name;
    private PoolGame.BallTypes myBallType;
    private int score;

    public PoolPlayer(String name) {
        this.name = name;
        myBallType = null;
        score = 0;
    }

    public PoolGame.BallTypes getBallType(){
        return myBallType;
    }

    public void setMyBallType(PoolGame.BallTypes newBallType){
        this.myBallType = newBallType;
    }

    public String getName(){
        return name;
    }

    public int getScore(){
        return score;
    }

    public int incScore(){
        return score++;
    }

    /**
     * Pot a ball of type @ballTypePotted
     * @param ballTypePotted
     * @return whether the pot was a foul or not
     */
    public boolean potBall(PoolGame.BallTypes ballTypePotted){
        boolean foulBall = !ballTypePotted.equals(myBallType);
        if(!foulBall){
            score++;
        }
        return foulBall;
    }
}
