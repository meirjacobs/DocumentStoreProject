package edu.yu.cs.com1320.project.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

public class RandomTests {
    @Test
    public void uritest() throws URISyntaxException {
        URI uri = new URI("stringvalue");
        String string = uri.toString();
        URI uri2 = new URI(string);
        assertEquals(uri, uri2);
    }
}
