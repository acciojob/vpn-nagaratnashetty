package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();

        if(user.getMaskedIp()!=null)
            throw new Exception("Already connected");
        else if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())) {
            return user;
        } else{
            if(user.getServiceProviderList()==null){
                throw new Exception("Unable to connect");
            }

            List<ServiceProvider> providers = user.getServiceProviderList();
            int min = Integer.MAX_VALUE;
            ServiceProvider serviceProvider1 = null;
            Country country1 = null;

            for(ServiceProvider serviceProvider:providers){
                List<Country> countryList = serviceProvider.getCountryList();

                for (Country country:countryList){

                    if(countryName.equalsIgnoreCase(country.getCountryName().toString()) && min>serviceProvider.getId()){
                        min=serviceProvider.getId();
                        serviceProvider1=serviceProvider;
                        country1=country;
                    }
                }
            }
            if(serviceProvider1!=null){
                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(serviceProvider1);

                String countryCode = country1.getCode();
                int providerId = serviceProvider1.getId();
                String masked = countryCode + "." + providerId +"."+ userId;

                user.setMaskedIp(masked);
                user.setConnected(true);
                user.getConnectionList().add(connection);

                serviceProvider1.getConnectionList().add(connection);

                userRepository2.save(user);
                serviceProviderRepository2.save(serviceProvider1);

                return user;
            }
            else
                throw new Exception("Unable to connect");
        }

    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(user.getConnected()==false)
            throw new Exception("Already disconnected");

        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;

    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        if (receiver.getMaskedIp() != null) {
            String Ip = receiver.getMaskedIp();

            String code = Ip.substring(0, 3);

            if (code.equals(sender.getOriginalCountry().getCode()))
                return sender;
            else {
                String countryName = "";

                if (code.equals(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();
                if (code.equals(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();
                if (code.equals(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();
                if (code.equals(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();
                if (code.equals(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();

//                User updatedSender = connect(senderId,countryName);
//                if(!updatedSender.getConnected())
//                    throw new Exception("Cannot establish communication");
//                else
//                    return updatedSender;
                try {
                    User updatedSender = connect(senderId, countryName);
                    return updatedSender;
                } catch (Exception e) {
                    throw new Exception("Cannot establish communication");
                }
            }
        } else {
            if (receiver.getOriginalCountry().equals(sender.getOriginalCountry())) {
                return sender;
            } else {
                String countryName = receiver.getOriginalCountry().getCountryName().toString();
//                User updatedSender = connect(senderId,countryName);
//                if(!updatedSender.getConnected())
//                    throw new Exception("Cannot establish communication");
//                else
//                    return updatedSender;
                try {
                    User updatedSender = connect(senderId, countryName);
                    return updatedSender;
                } catch (Exception e) {
                    throw new Exception("Cannot establish communication");
                }
            }
        }
    }
}
