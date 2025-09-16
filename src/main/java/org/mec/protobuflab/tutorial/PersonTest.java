package org.mec.protobuflab.tutorial;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.mec.protobuflab.tutorial.protos.AddressBookProtos;

import java.util.List;

public class PersonTest
{
    public static void main(String[] args) throws InvalidProtocolBufferException
    {
        AddressBookProtos.Person joe = AddressBookProtos.Person.newBuilder()
                .setName("Joe")
                .setId(102)
                .setEmail("joe@gmail.com")
                .addAllPhones(
                        List.of(AddressBookProtos.Person.PhoneNumber.newBuilder()
                                .setNumber("12345678")
                                .setType(AddressBookProtos.Person.PhoneType.PHONE_TYPE_MOBILE)
                                .build()))
                .build();

        AddressBookProtos.Person bill = AddressBookProtos.Person.newBuilder()
                .setName("Bill")
                .setId(204)
                .setEmail("bill@outlook.com")
                .addAllPhones(
                        List.of(
                                AddressBookProtos.Person.PhoneNumber.newBuilder()
                                .setNumber("333888147")
                                .setType(AddressBookProtos.Person.PhoneType.PHONE_TYPE_MOBILE)
                                .build(),
                                AddressBookProtos.Person.PhoneNumber.newBuilder()
                                .setNumber("3456789")
                                .setType(AddressBookProtos.Person.PhoneType.PHONE_TYPE_HOME)
                                .build()))
                .build();

        var addressBook = AddressBookProtos.AddressBook.newBuilder()
                .addAllPeople(List.of(joe, bill))
                .build();

        System.out.println("joe=" + joe);
        System.out.println("bill=" + bill);
        System.out.println("addressBook=" + addressBook);

        // Another message
        AddressBookProtos.Person pete = AddressBookProtos.Person.newBuilder()
                .setName("Pete")
                .setId(208)
                .setEmail("pete@gmail.com")
                .addAllPhones(
                        List.of(AddressBookProtos.Person.PhoneNumber.newBuilder()
                                .setNumber("888777666")
                                .setType(AddressBookProtos.Person.PhoneType.PHONE_TYPE_WORK)
                                .build()))
                .build();

        // Create address2 and merge with addressBook above
        var address2 = AddressBookProtos.AddressBook.newBuilder()
                .mergeFrom(addressBook)
                .addPeople(pete)
                .build();

        System.out.println("address2=" + address2);

        // Convert address2 message to byteString
        ByteString address2ByteString = address2.toByteString();
        System.out.println("address2ByteString.size() =" + address2ByteString.size());
        System.out.println("address byte[] length=" + address2.toByteArray().length);

        // Reading the data back
        AddressBookProtos.AddressBook decoded = AddressBookProtos.AddressBook.parseFrom(address2ByteString);
        System.out.println("decode message=" + decoded);
    }
}
