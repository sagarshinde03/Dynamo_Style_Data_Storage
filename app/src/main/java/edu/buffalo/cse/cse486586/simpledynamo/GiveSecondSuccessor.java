package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sagar on 4/23/15.
 */
public class GiveSecondSuccessor implements Serializable {
    public Map<String, String> mapToBeSentSecondSuccessor = new HashMap<String, String>();
}
