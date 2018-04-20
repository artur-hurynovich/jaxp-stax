package jaxp.stax;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
class Car {
    private String mark;
    private String model;
    private TechChar techChar;
    public static class TechChar {
        private XMLGregorianCalendar date;
        private double engineCapacity;
        private String engineType;
        void setDate(LocalDate date) {
            try {
                this.date = DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toString());
            }
            catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }
        }
        void setEngineCapacity(double engineCapacity) {
            this.engineCapacity = engineCapacity;
        }
        void setEngineType(String engineType) {
            this.engineType = engineType;
        }
        XMLGregorianCalendar getDate() {
            return date;
        }
        double getEngineCapacity() {
            return engineCapacity;
        }
        String getEngineType() {
            return engineType;
        }
        @Override
        public String toString() {
            return date.toGregorianCalendar().toZonedDateTime().toLocalDate().
                format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) + ", " + 
                engineCapacity + " (" + engineType + ")";
        }
    }
    void setMark(String mark) {
        this.mark = mark;
    }
    void setModel(String model) {
        this.model = model;
    }
    void setTechChar(TechChar techChar) {
        this.techChar = techChar;
    }
    String getMark() {
        return mark;
    }
    String getModel() {
        return model;
    }
    TechChar getTechChar() {
        if (techChar == null) {
            return new TechChar();
        }
        else {
            return techChar;
        }
    }
    @Override
    public String toString() {
        return mark + " " + model + ": " + techChar.toString();
    }
}
class CarBuilder {
    static Car buildCar(String mark, String model, LocalDate date, double engineCapacity, String engineType) {
        Car car = new Car();
        car.setMark(mark);
        car.setModel(model);
        Car.TechChar techChar = car.getTechChar();
        techChar.setDate(date);
        techChar.setEngineCapacity(engineCapacity);
        techChar.setEngineType(engineType);
        car.setTechChar(techChar);
        return car;
    }
}
class CarElementBuilder {
    static void buildCarElement(Car car, XMLStreamWriter streamWriter) {
        try {
            streamWriter.writeStartElement("car");
            streamWriter.writeStartElement("mark");
            streamWriter.writeCharacters(car.getMark());
            streamWriter.writeEndElement();
            streamWriter.writeStartElement("model");
            streamWriter.writeCharacters(car.getModel());
            streamWriter.writeEndElement();
            streamWriter.writeStartElement("characteristics");
            streamWriter.writeStartElement("date");
            streamWriter.writeCharacters(car.getTechChar().getDate().toString());
            streamWriter.writeEndElement();
            streamWriter.writeStartElement("engineCapacity");
            streamWriter.writeCharacters(String.valueOf(car.getTechChar().getEngineCapacity()));
            streamWriter.writeEndElement();
            streamWriter.writeStartElement("engineType");
            streamWriter.writeCharacters(car.getTechChar().getEngineType());
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
public class StAXClass {
    public static void main(String[] args) {
        ArrayList<Car> cars = new ArrayList<Car>() {
            {
                add(CarBuilder.buildCar("Toyota", "FJCruiser", LocalDate.of(2017, 7, 5),
                        4.7, "Gasoline"));
                add(CarBuilder.buildCar("Ford", "F-150", LocalDate.of(2015, 3, 19),
                        6.0, "Gasoline"));
                add(CarBuilder.buildCar("Mercedes", "E320", LocalDate.of(2005, 2, 21),
                        3.2, "Diesel"));
                add(CarBuilder.buildCar("Dodge", "Charger", LocalDate.of(2012, 12, 31),
                        5.0, "Gasoline"));
            }
        };
        try (PrintWriter writer = new PrintWriter("cars.xml")){
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
            streamWriter.writeStartDocument("UTF-16", "1.0");
            streamWriter.writeStartElement("cars");
            cars.forEach(car -> CarElementBuilder.buildCarElement(car, streamWriter));
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
            streamWriter.flush();
        }
        catch (FileNotFoundException |XMLStreamException e) {
            e.printStackTrace();
        }
        try (FileReader reader = new FileReader("cars.xml")) {
            ArrayList<String> newCars = new ArrayList<>();
            String mark = null;
            String model = null;
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
            while (streamReader.hasNext()) {
                streamReader.next();
                if (streamReader.isStartElement() && streamReader.getLocalName().equalsIgnoreCase("mark")) {
                    streamReader.next();
                    mark = streamReader.getText();
                }
                if (streamReader.isStartElement() && streamReader.getLocalName().equalsIgnoreCase("model")) {
                    streamReader.next();
                    model = streamReader.getText();
                }
                if (streamReader.isStartElement() && streamReader.getLocalName().equalsIgnoreCase("engineCapacity")) {
                    streamReader.next();
                    if (Double.valueOf(streamReader.getText()) > 4.5) {
                        newCars.add(mark + " " + model);
                    }
                }
            }
            System.out.println(newCars);
        }
        catch (IOException |XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
