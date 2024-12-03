package Model;
public class Customer {
    private String id;
    private String name;
    private String dateOfBirth;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String createdDate;

    public String getId() {
        return id;
    }



    public Customer setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public Customer setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Customer setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public Customer setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        return this;
    }

    public String getCity() {
        return city;
    }

    public Customer setCity(String city) {
        this.city = city;
        return this;
    }

    public String getState() {
        return state;
    }

    public Customer setState(String state) {
        this.state = state;
        return this;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Customer setZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public Customer setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
        return this;
    }
}
