// HotelManagement.java
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class Room {
    int roomNumber;
    String roomType;
    double pricePerNight;
    String status; // Available, Occupied, Reserved, Maintenance
    LocalDate availableFrom;

    public Room(int roomNumber, String roomType, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = "Available";
        this.availableFrom = LocalDate.now();
    }

    @Override
    public String toString() {
        return "غرفة " + roomNumber + " | نوع: " + roomType + " | سعر: " + pricePerNight +
               " | حالة: " + status + " | متاحة من: " + availableFrom;
    }
}

class Guest {
    String fullName;
    String uniqueCode;

    public Guest(String fullName) {
        this.fullName = fullName;
        this.uniqueCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String toString() {
        return "الاسم: " + fullName + " | الكود: " + uniqueCode;
    }
}

class Reservation {
    Guest guest;
    Room room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;
    String reservationId;

    public Reservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = "Confirmed";
        this.reservationId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double totalPrice() {
        return nights() * room.pricePerNight;
    }

    @Override
    public String toString() {
        return "حجز رقم: " + reservationId + "\n" +
               "العميل: " + guest + "\n" +
               "الغرفة: " + room.roomNumber + " (" + room.roomType + ")\n" +
               "من: " + checkIn + " إلى: " + checkOut + " (" + nights() + " ليالي)\n" +
               "الإجمالي: " + totalPrice() + " جنيه";
    }
}

class HotelSystem {
    String name;
    List<Room> rooms = new ArrayList<>();
    List<Reservation> reservations = new ArrayList<>();
    List<Guest> guests = new ArrayList<>();

    public HotelSystem(String name) {
        this.name = name;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getAvailableRooms(String roomType, LocalDate checkIn) {
        if (checkIn == null) checkIn = LocalDate.now();
        LocalDate finalCheckIn = checkIn;

        return rooms.stream()
                .filter(r -> (r.status.equals("Available") || r.status.equals("Reserved")))
                .filter(r -> !r.availableFrom.isAfter(finalCheckIn))
                .filter(r -> roomType == null || r.roomType.equalsIgnoreCase(roomType))
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsBecomingAvailable(int daysAhead) {
        LocalDate target = LocalDate.now().plusDays(daysAhead);
        return rooms.stream()
                .filter(r -> r.status.equals("Occupied") && !r.availableFrom.isAfter(target))
                .collect(Collectors.toList());
    }

    public Reservation createReservation(String fullName, int roomNumber,
                                         LocalDate checkIn, LocalDate checkOut) {
        Room room = rooms.stream()
                .filter(r -> r.roomNumber == roomNumber)
                .findFirst()
                .orElse(null);

        if (room == null) {
            System.out.println("الغرفة غير موجودة");
            return null;
        }

        if (!(room.status.equals("Available") || room.status.equals("Reserved")) ||
            room.availableFrom.isAfter(checkIn)) {
            System.out.println("الغرفة غير متاحة في هذا التاريخ");
            return null;
        }

        Guest guest = new Guest(fullName);
        guests.add(guest);

        Reservation reservation = new Reservation(guest, room, checkIn, checkOut);
        reservations.add(reservation);

        room.status = "Reserved";
        room.availableFrom = checkOut;

        System.out.println("تم الحجز بنجاح!");
        System.out.println(reservation);
        return reservation;
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== نظام إدارة فندق " + name + " =====");
            System.out.println("1. عرض الغرف المتاحة الآن");
            System.out.println("2. عرض الغرف المتاحة بتاريخ محدد");
            System.out.println("3. عرض الغرف اللي هتفضي قريب");
            System.out.println("4. عمل حجز جديد");
            System.out.println("5. عرض كل الحجوزات");
            System.out.println("0. خروج");
            System.out.print("اختر: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    List<Room> avail = getAvailableRooms(null, null);
                    if (avail.isEmpty()) System.out.println("لا توجد غرف متاحة");
                    else avail.forEach(System.out::println);
                    break;

                case "2":
                    System.out.print("تاريخ الوصول (YYYY-MM-DD): ");
                    String dateStr = sc.nextLine();
                    System.out.print("نوع الغرفة (أو اتركه فاضي): ");
                    String type = sc.nextLine().trim();
                    if (type.isEmpty()) type = null;
                    try {
                        LocalDate checkIn = LocalDate.parse(dateStr);
                        List<Room> rooms = getAvailableRooms(type, checkIn);
                        if (rooms.isEmpty()) System.out.println("لا توجد غرف");
                        else rooms.forEach(System.out::println);
                    } catch (Exception e) {
                        System.out.println("خطأ في التاريخ");
                    }
                    break;

                case "3":
                    System.out.print("كام يوم قدام؟ ");
                    int days = Integer.parseInt(sc.nextLine().isEmpty() ? "7" : sc.nextLine());
                    List<Room> becoming = getRoomsBecomingAvailable(days);
                    if (becoming.isEmpty()) System.out.println("مفيش");
                    else becoming.forEach(System.out::println);
                    break;

                case "4":
                    System.out.print("اسم العميل (ثنائي): ");
                    String name = sc.nextLine();
                    System.out.print("رقم الغرفة: ");
                    int roomNum = Integer.parseInt(sc.nextLine());
                    System.out.print("تاريخ الوصول (YYYY-MM-DD): ");
                    LocalDate cin = LocalDate.parse(sc.nextLine());
                    System.out.print("تاريخ المغادرة (YYYY-MM-DD): ");
                    LocalDate cout = LocalDate.parse(sc.nextLine());
                    if (!cout.isAfter(cin)) {
                        System.out.println("تاريخ المغادرة غلط");
                        break;
                    }
                    createReservation(name, roomNum, cin, cout);
                    break;

                case "5":
                    if (reservations.isEmpty()) System.out.println("لا توجد حجوزات");
                    else reservations.forEach(r -> {
                        System.out.println("--------------------------------");
                        System.out.println(r);
                    });
                    break;

                case "0":
                    System.out.println("شكرًا");
                    return;

                default:
                    System.out.println("اختيار غلط");
            }
        }
    }

    public static void main(String[] args) {
        HotelSystem hotel = new HotelSystem("فندق النيل الفاخر");

        hotel.addRoom(new Room(101, "Single", 500));
        hotel.addRoom(new Room(102, "Single", 500));
        hotel.addRoom(new Room(201, "Double", 800));
        hotel.addRoom(new Room(202, "Double", 850));
        hotel.addRoom(new Room(301, "Suite", 1500));
        hotel.addRoom(new Room(302, "Suite", 1800));

        // غرفة مشغولة مثال
        hotel.rooms.get(0).status = "Occupied";
        hotel.rooms.get(0).availableFrom = LocalDate.now().plusDays(3);

        hotel.showMenu();
    }
}