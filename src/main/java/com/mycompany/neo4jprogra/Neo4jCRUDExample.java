/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.neo4jprogra;

import java.sql.Driver;
import javax.naming.spi.DirStateFactory.Result;
import org.neo4j.driver.*;
/**
 *
 * @author Samuel GR
 */

 
public class Neo4jCRUDExample {
    
    private final static String URI = "bolt://localhost:7687";
    private final static String USER = "neo4j";
    private final static String PASSWORD = "your_password";

    private Driver driver;

    public Neo4jCRUDExample() {
        driver = (Driver) GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    public void createGenre(String name, String description) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(
                    "CREATE (g:Genre {name: $name, description: $description})",
                    Values.parameters("name", name, "description", description)
            ));
        }
    }

    public void createGame(String name, int year, String genre) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(
                    "MATCH (g:Genre {name: $genre}) " +
                    "CREATE (j:Game {name: $name, year: $year})-[:PERTENECE_A]->(g)",
                    Values.parameters("name", name, "year", year, "genre", genre)
            ));
        }
    }

    public void updateGenre(String name, String newDescription) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(
                    "MATCH (g:Genre {name: $name}) SET g.description = $newDescription",
                    Values.parameters("name", name, "newDescription", newDescription)
            ));
        }
    }

    public void deleteGenre(String name) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(
                    "MATCH (g:Genre {name: $name}) DETACH DELETE g",
                    Values.parameters("name", name)
            ));
        }
    }

    public void printGames() {
        try (Session session = driver.session()) {
            Result result = session.readTransaction(tx ->
                    tx.run("MATCH (j:Game)-[:PERTENECE_A]->(g:Genre) " +
                            "RETURN j.name AS name, j.year AS year, g.name AS genre"));

            while (result.hasNext()) {
                Record record = result.next();
                String name = record.get("name").asString();
                int year = record.get("year").asInt();
                String genre = record.get("genre").asString();

                System.out.println("Game: " + name);
                System.out.println("Year: " + year);
                System.out.println("Genre: " + genre);
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        Neo4jCRUDExample example = new Neo4jCRUDExample();

        example.createGenre("Terror", "Género de videojuegos de terror");
        example.createGenre("Aventura", "Género de videojuegos de aventura");
        example.createGenre("Puzzle", "Género de videojuegos de puzzle");

        example.createGame("Resident Evil 7", 2017, "Terror");
        example.createGame("The Legend of Zelda: Breath of the Wild", 2017, "Aventura");
        example.createGame("Portal 2", 2011, "Puzzle");

        example.printGames();

        example.updateGenre("Terror", "Juegos de terror y suspenso");
        example.printGames();

        example.deleteGenre("Puzzle");
        example.printGames();

        example.driver.close();
    }
}