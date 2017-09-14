package com.example;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.*;

public class CreateIndex {
	
	private Client client;
	private Node node;
	
	public void startNode()
	{
		//starting the node
		node  = NodeBuilder.nodeBuilder().clusterName("mycluster").node();
		client = node.client();
	}
	
	public void stopNode()
	{
		//closing the node
		node.close();
	}
	

	
	

}
