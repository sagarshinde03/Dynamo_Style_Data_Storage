package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sagar on 4/24/15.
 */
public class FrwrdMessagesToFirstSuccessor implements Serializable {
    public Map<String, String> mapToForward = new HashMap<String, String>();
}
