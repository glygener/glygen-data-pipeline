package uk.ac.ebi.uniprot.glygen;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class TestNeo4jConnection {
    public static void main(String[] args) {
        try (Driver driver = GraphDatabase.driver("bolt://neo4j:7687",
                AuthTokens.basic("neo4j", "reactome"))) {
            Session session = driver.session();
            String greeting = session.writeTransaction(tx ->
                    tx.run("RETURN 'Hello, Neo4j!'").single().get(0).asString());
            System.out.println(greeting);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}