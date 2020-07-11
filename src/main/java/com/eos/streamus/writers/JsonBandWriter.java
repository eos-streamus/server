package com.eos.streamus.writers;

import com.eos.streamus.models.Band;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JsonBandWriter extends JsonArtistWriter {
  private static class JsonBandMemberWriter extends JsonMusicianWriter {
    /** {@link com.eos.streamus.models.Band .Member} to write as Json. */
    private final Band.Member member;

    JsonBandMemberWriter(final Band.Member member) {
      super(member.getMusician());
      this.member = member;
    }

    @Override
    protected JsonNode getSpecificJson(final ObjectNode objectNode) {
      super.getSpecificJson(objectNode);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      objectNode.put("from", dateFormat.format(member.getFrom()));
      if (member.getTo() != null) {
        objectNode.put("to", dateFormat.format(member.getTo()));
      }
      return objectNode;
    }

  }

  /** {@link com.eos.streamus.models.Band} to write as Json. */
  private final Band band;

  public JsonBandWriter(final Band band) {
    super(band);
    this.band = band;
  }

  @Override
  protected final JsonNode addSpecificArtistJson(final ObjectNode objectNode) {
    ArrayNode members = objectNode.putArray("members");

    for (Band.Member member : band.getMembers()) {
      members.add(new JsonBandMemberWriter(member).getJson());
    }

    return objectNode;
  }

}
