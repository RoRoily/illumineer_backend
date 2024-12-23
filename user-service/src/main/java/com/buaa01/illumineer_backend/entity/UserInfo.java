package com.buaa01.illumineer_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    private Person person;
    @JsonProperty("activities-summary")
    private ActivitiesSummary activitiesSummary;

    // Getters
    public Person getPerson() {
        return person;
    }

    public ActivitiesSummary getActivitiesSummary() {
        return activitiesSummary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Person {
        private Name name;

        // Getters
        public Name getName() {
            return name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        @JsonProperty("given-names")
        private ValueWrapper givenNames;
        @JsonProperty("family-name")
        private ValueWrapper familyName;

        // Getters
        public ValueWrapper getGivenNames() {
            return givenNames;
        }

        public ValueWrapper getFamilyName() {
            return familyName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueWrapper {
        private String value;

        // Getter
        public String getValue() {
            return value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivitiesSummary {
        private Employments employments;

        // Getter
        public Employments getEmployments() {
            return employments;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Employments {
        @JsonProperty("affiliation-group")
        private AffiliationGroup[] affiliationGroups;

        // Getter
        public AffiliationGroup[] getAffiliationGroups() {
            return affiliationGroups;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AffiliationGroup {
        private Summary[] summaries;

        // Getter
        public Summary[] getSummaries() {
            return summaries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        @JsonProperty("employment-summary")
        private EmploymentSummary employmentSummary;

        // Getter
        public EmploymentSummary getEmploymentSummary() {
            return employmentSummary;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmploymentSummary {
        private Organization organization;

        // Getter
        public Organization getOrganization() {
            return organization;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Organization {
        private String name;
        private Address address;

        // Getters
        public String getName() {
            return name;
        }

        public Address getAddress() {
            return address;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String city;
        private String country;

        // Getter
        public String getCity() {
            return city;
        }

        // Getter
        public String getCountry() {
            return country;
        }
    }
}
