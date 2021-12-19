package com;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

class Node{

    // A Graph Node,
    // have a unique name
    // have list of neighbours Node

    String name;
    List<Node> neighbours;

    public Node(String name){
        this.name = name;
        this.neighbours = new ArrayList<>();
    }
    void addNeighbour(List<Node> nodes){
        for(Node n : nodes){
            if(!neighbours.contains(n) && !name.equals(n.name)){
                neighbours.add(n);
            }
        }
    }
    public List<Node> getNeighbours(){
        return neighbours;
    }

}

public class Main {

    List<Node> nodes = new ArrayList<>();
    Hashtable<Node,Boolean> recommended  = new Hashtable<>();

    public List<String> readData()  throws IOException {
        // get the html from url
        Document doc = Jsoup.connect("https://top40weekly.com/2018-all-charts/").get();
        System.out.println(doc.title());

        // select all the class .x-text p
        Elements elements = doc.select(".x-text p");

        List<String> songs = new ArrayList<>();

        // loop through all the element
        for(Element element : elements){

            String elStr = element.toString();
            // ignore element that not contain song information
            if(elStr.contains("iframe") || elStr.contains("<a") || elStr.contains("<span") || elStr.contains("TW LW TITLE")){
                continue;
            }

            // remote p tag,
            elStr = elStr.replaceAll("</p>","");
            elStr = elStr.replaceAll("<p>","");
            // split the song in the p tag
            String[] songsStr = elStr.split("<br>");

            for(String songStr : songsStr){
                songStr = songStr.trim();
                songs.add(songStr);
            }
        }
        return songs;
    }

    public  List<String> getArtistStrs() throws IOException {

        List<String> songs = readData();
        List<String> artists = new ArrayList<>();

        for(String song : songs){
            // get the artist information
            String artistRaw = song.split("–•–")[1];
            int cutIndex = artistRaw.indexOf('(');
            String artistStr = artistRaw.substring(0,cutIndex);


            // replace any sign of featuring -> *
            // ex : imagine dragon and Zedd => imagine dragon * Zedd

            artistStr = artistStr.replaceAll(" \\+ ","\\*");
            artistStr = artistStr.replaceAll(" featuring ","\\*");
            artistStr = artistStr.replaceAll(", ","\\*");
            artistStr = artistStr.replaceAll(" X ","\\*");
            artistStr = artistStr.replaceAll(" x ","\\*");
            artistStr = artistStr.replaceAll("x ","\\*");
            artistStr = artistStr.replaceAll(" and ","\\*");
            artistStr = artistStr.replaceAll(" &amp; ","\\*");


            artists.add(artistStr);
        }

        return artists;
    }

    Node getNode(String name){
        for(Node n : nodes){
            if(n.name.equals(name)){
                return n;
            }
        }
        return null;
    }

    void initNodes() throws IOException {

        List<String> artistsSt = getArtistStrs();

        for(String str : artistsSt){
            // Split * to get all the artist in a song
            String[] artistsStr = str.split("\\*");
            List<Node> neighbours = new ArrayList<>();

            // loop all the name of artists in the song
            for(String name : artistsStr){
                name = name.trim();

                // add node
                // find if listNode already have artist name
                Node existNode = getNode(name);
                if(existNode == null){ // if node add it
                    existNode = new Node(name);
                    nodes.add(existNode);
                }
                neighbours.add(existNode); // add the node to the neighbours list
            }
            // add neighbours
            // loop al the name again then add the neighbours to it
            for(String name : artistsStr){
                name = name.trim();
                Node existNode = getNode(name);
                existNode.addNeighbour(neighbours);
            }

        }
    }

    private void printNeighbours(Node node){
        // if A node have 0 neighbours
        if(node.getNeighbours().isEmpty()){
            return;
        }
        System.out.println("****Recommend for " + node.name + " :");
        recommended.put(node,true); // set the recommended = true for the current Artist's name

        for(Node n : node.getNeighbours()){
            System.out.println("        +) " + n.name);
        }

        // recursive print the neighbour's neighbours, .. etc
        for(Node n : node.getNeighbours()){
            if(!recommended.get(n)){
                printNeighbours(n);
            }
        }
    }

    public void recommend(String name){

        // set the recommended = false for all artist
        recommended  = new Hashtable<>();
        for(Node n : nodes){
            recommended.put(n,false);
        }


        // find the node that have artist name
        Node foundNode = null;
        for(Node n : nodes){
            if(n.name.equals(name)){
                foundNode = n;
            }
        }

        // print the node
        if(foundNode != null){
            if(foundNode.getNeighbours().isEmpty()){
                System.out.println("No recommend!!");
                return;
            }
            printNeighbours(foundNode);
        }
        else {
            System.out.println("No recommend!!");
        }
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.initNodes();

        String name;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter name of an artist: ");
        name = scanner.nextLine();

        main.recommend(name);

    }
}
