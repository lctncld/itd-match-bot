package com.epam.match.domain;

public enum Gender {
  MALE("gender.male"),
  FEMALE("gender.female"),
  BOTH("gender.both");

  private final String localization;

  Gender(String localization) {
    this.localization = localization;
  }

  public String getLocalization() {
    return localization;
  }
}
