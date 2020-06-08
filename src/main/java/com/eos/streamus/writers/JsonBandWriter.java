package com.eos.streamus.writers;

import com.eos.streamus.models.Band;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonBandWriter extends JsonArtistWriter {
  private static class JsonBandMemberWriter extends JsonMusicianWriter {
    private final Band.Member member;

    public JsonBandMemberWriter(final Band.Member member) {
      super(member.getMusician());
      this.member = member;
    }

    @Override
    protected JsonNode getSpecificJson(final ObjectNode objectNode) {
      super.getSpecificJson(objectNode);
      objectNode.put("from", member.getFrom().getTime());
      if (member.getTo() != null) {
        objectNode.put("to", member.getTo().getTime());
      }
      return objectNode;
    }

  }

  private final Band band;

  public JsonBandWriter(final Band band) {
    super(band);
    this.band = band;
  }

  @Override
  protected JsonNode addSpecificArtistJson(final ObjectNode objectNode) {
    ArrayNode members = objectNode.putArray("members");

    for (Band.Member member : band.getMembers()) {
      members.add(new JsonBandMemberWriter(member).getJson());
    }

    return objectNode;
  }

}
