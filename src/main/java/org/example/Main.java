package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyBot());
            System.out.println("✅ Bot ishga tushdi!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MyBot extends TelegramLongPollingBot {
        private static final String BOT_USERNAME = "@Uzbek_cat_bot";
        private static final String BOT_TOKEN = System.getenv("BOT_TOKEN") != null ?
                System.getenv("BOT_TOKEN") : "8577521489:AAGbp2MvcMXZlnK-KbDdmPm8WArYlJ4PxWk";

        private final long ADMIN_ID = 673018191L;
        private final long TECHNICAL_ADMIN_ID = 7038296036L;
        private final Set<Long> ADMIN_IDS = Set.of(ADMIN_ID, TECHNICAL_ADMIN_ID);
        private final String CHANNEL_USERNAME = "@uzbek_cats";

        // Bot holati (on/off)
        private boolean botEnabled = true;
        private final Set<Long> bannedUsers = new HashSet<>();

        // State va ma'lumotlar xaritalari
        private final Map<Long, String> stateMap = new ConcurrentHashMap<>();
        private final Map<Long, List<String>> photosMap = new ConcurrentHashMap<>();
        private final Map<Long, String> manzilMap = new ConcurrentHashMap<>();
        private final Map<Long, String> phoneMap = new ConcurrentHashMap<>();
        private final Map<Long, String> breedMap = new ConcurrentHashMap<>();
        private final Map<Long, String> ageMap = new ConcurrentHashMap<>();
        private final Map<Long, String> healthMap = new ConcurrentHashMap<>();
        private final Map<Long, String> genderMap = new ConcurrentHashMap<>();
        private final Map<Long, String> priceMap = new ConcurrentHashMap<>();
        private final Map<Long, String> checkMap = new ConcurrentHashMap<>();
        private final Map<Long, String> adTypeMap = new ConcurrentHashMap<>();
        private final Map<Long, Integer> mushukSoniMap = new ConcurrentHashMap<>();
        private final Map<Long, String> sterilizationMap = new ConcurrentHashMap<>();
        private final Map<Long, String> platformaMap = new ConcurrentHashMap<>();
        private final Map<Long, String> valyutaMap = new ConcurrentHashMap<>();
        private final Map<Long, String> mediaTypeMap = new ConcurrentHashMap<>();
        private final Map<Long, Boolean> userHasPendingAdMap = new ConcurrentHashMap<>();
        private final Map<Long, String> adTextMap = new ConcurrentHashMap<>();
        private final Map<Long, String> adPhotoMap = new ConcurrentHashMap<>();
        private final Map<Long, String> declineReasonsMap = new ConcurrentHashMap<>();
        private final Map<Integer, Long> adminMessageIds = new ConcurrentHashMap<>();
        private final Map<Long, Long> adminEditUserIdMap = new ConcurrentHashMap<>();
        private final Map<String, List<AdRecord>> statisticsMap = new ConcurrentHashMap<>();
        private final Map<Long, String> userUsernameMap = new ConcurrentHashMap<>();
        private final Map<Long, String> userPhones = new ConcurrentHashMap<>();

        // User ads map
        private final Map<Long, List<UserAd>> userAdsMap = new ConcurrentHashMap<>();

        // Foydalanuvchi ma'lumotlari
        private final Map<Long, String> userFullNameMap = new ConcurrentHashMap<>();
        private final Map<Long, Boolean> userRegisteredMap = new ConcurrentHashMap<>();
        private final Map<Long, Date> lastAdTimeMap = new ConcurrentHashMap<>();
        private final Map<Long, Integer> userNumberMap = new ConcurrentHashMap<>();
        private final Map<Long, Integer> userAdCountMap = new ConcurrentHashMap<>();
        private final AtomicInteger userCounter = new AtomicInteger(1);

        // Referral tizimi
        private final Map<Long, Integer> userScores = new ConcurrentHashMap<>();
        private final Map<Long, Long> referralMap = new ConcurrentHashMap<>();
        private final Map<Long, String> referralCodes = new ConcurrentHashMap<>();
        private final Map<String, Long> codeToUserMap = new ConcurrentHashMap<>();

        // Konkurs
        private String currentKonkursImageUrl = "https://i.postimg.cc/YvGp1gHt/image.jpg";
        private String currentKonkursText = "🎁 Scottish fold black\n\nSiz toplagan ovoz ochib ketmaydi toki 🏆 g'olib bo'lgungizgacha 💯";

        // Bloklangan so'zlar
        private final Set<String> bannedWords = Set.of(
                "mushuk sotiladi", "mushuk bor", "sotaman", "sotiladi", "bor",
                "sotuvda", "arzonga mushuk", "hadiyaga", "mendayam bor",
                "atrofida bor", "sotman", "kimga mushuk kerak", "Mandayam bor",
                "beramiz", "beraman", "сотаман", "мушук бор", "бор", "бера",
                "hadyaga", "кимга мушук керак", "мушук сотилади"
        );

        private final String WARNING_MESSAGE = "❌ Iltimos, reklama tarqatmang!";

        // Viloyatlar va yoshlar
        private final List<String> viloyatlar = Arrays.asList(
                "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Xorazm",
                "Namangan", "Navoiy", "Qashqadaryo", "Samarqand",
                "Sirdaryo", "Surxondaryo", "Toshkent", "Toshkent shahar"
        );

        private final List<String> ages = Arrays.asList(
                "1 oylik", "2 oylik", "3 oylik", "4 oylik", "5 oylik",
                "6 oylik", "7 oylik", "8 oylik", "9 oylik", "10 oylik",
                "11 oylik", "1 yosh", "1,5 yosh", "2 yosh", "2,5 yosh",
                "3 yosh", "3,5 yosh", "4 yosh", "4,5 yosh", "5 yosh"
        );

        // Mushuk zotlari
        private final Map<Integer, List<String>> breedPages = new HashMap<>();
        private final List<String> allBreeds = new ArrayList<>();

        // Konkurs ishtirokchilari
        private List<KonkursParticipant> konkursParticipants = new ArrayList<>();

        // E'lon ID
        private final AtomicLong adIdCounter = new AtomicLong(1000);

        // KLASSLAR
        class AdRecord {
            long userId;
            String username;
            String adType;
            String breed;
            Date date;
            String phone;
            int adNumber;

            public AdRecord(long userId, String username, String adType, String breed, String phone, int adNumber) {
                this.userId = userId;
                this.username = username;
                this.adType = adType;
                this.breed = breed;
                this.phone = phone;
                this.adNumber = adNumber;
                this.date = new Date();
            }

            public String getFormattedDate() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                return sdf.format(date);
            }
        }

        class UserAd {
            long adId;
            String adType;
            List<String> photos;
            String breed;
            String age;
            String gender;
            String health;
            String sterilization;
            String manzil;
            String phone;
            String price;
            String valyuta;
            Date createdDate;
            boolean isActive;
            int adNumber;
            Integer channelMessageId;

            public UserAd(long adId, String adType, List<String> photos, String breed,
                          String age, String gender, String health, String sterilization,
                          String manzil, String phone, String price, String valyuta, int adNumber) {
                this.adId = adId;
                this.adType = adType;
                this.photos = photos;
                this.breed = breed;
                this.age = age;
                this.gender = gender;
                this.health = health;
                this.sterilization = sterilization;
                this.manzil = manzil;
                this.phone = phone;
                this.price = price;
                this.valyuta = valyuta;
                this.adNumber = adNumber;
                this.createdDate = new Date();
                this.isActive = true;
                this.channelMessageId = null;
            }

            public boolean isExpired() {
                long tenDaysInMillis = 10 * 24 * 60 * 60 * 1000L;
                return new Date().getTime() - createdDate.getTime() > tenDaysInMillis;
            }

            public String getChannelLink() {
                if (channelMessageId != null) {
                    return "https://t.me/" + CHANNEL_USERNAME.replace("@", "") + "/" + channelMessageId;
                }
                return "https://t.me/" + CHANNEL_USERNAME.replace("@", "");
            }
        }

        static class KonkursParticipant {
            String name;
            String username;
            int score;
            String profileLink;

            public KonkursParticipant(String name, String username, int score, String profileLink) {
                this.name = name;
                this.username = username;
                this.score = score;
                this.profileLink = profileLink;
            }
        }

        public MyBot() {
            System.out.println("=== BOT ISHGA TUSHMOQDA ===");
            System.out.println("Admin IDs: " + ADMIN_IDS);
            System.out.println("Bot username: " + BOT_USERNAME);
            System.out.println("Channel username: " + CHANNEL_USERNAME);

            initializeBreedPages();

            statisticsMap.put("hadiya", new ArrayList<>());
            statisticsMap.put("sotish", new ArrayList<>());
            statisticsMap.put("vyazka", new ArrayList<>());

            // Map'larni ishga tushirish
            userScores.clear();
            referralMap.clear();

            initializeKonkursParticipants();
            startCleanupTimer();
            System.out.println("✅ Bot muvaffaqiyatli yuklandi!");
            System.out.println("==========================");
        }

        private void initializeBreedPages() {
            List<String> page1 = Arrays.asList(
                    "Scottish fold", "Scottish straight", "British blue", "British chinchilla",
                    "Bengal cat", "Turkish angora", "Scottish chinchila", "Gibrid", "British Shorthair"
            );
            breedPages.put(1, page1);
            allBreeds.addAll(page1);

            List<String> page2 = Arrays.asList(
                    "British Shorthair Britan qisqa junli", "British Longhair Britan uzun junli",
                    "Persian Cat Fors mushugi", "Maine Coon Meyn-kun", "Ragdoll Qo'g'irchoq mushuk",
                    "Bengal Cat Bengal mushugi", "Siberian Cat Sibir mushugi", "Russian blue Rus moviy mushugi"
            );
            breedPages.put(2, page2);
            allBreeds.addAll(page2);

            List<String> page3 = Arrays.asList(
                    "Abyssinian Abissiniya", "Norwegian Forest Cat Norvegiya", "Turkish Angora Turkiya angora",
                    "Turkish Van Turkiya Van", "Burmilla burma + chinchilla aralash", "British Longhair Britan uzun junli",
                    "American Shorthair Amerika qisqa junli", "American Curl Amerika bukilgan quloqli", "Egyptian Mau Misr mau mushugi"
            );
            breedPages.put(3, page3);
            allBreeds.addAll(page3);

            List<String> page4 = Arrays.asList(
                    "Tonkinese Tonkin siam va birma", "Balinese Balin uzun junli", "Exotic Shorthair Egzotik qisqa junli",
                    "Savannah Cat Savanna serval bilan aralash", "Munchkin kalta oyoqli mushuk", "Khao Manee Xao Mani",
                    "Uy mushuki", "Mushuk"
            );
            breedPages.put(4, page4);
            allBreeds.addAll(page4);
        }

        private void initializeKonkursParticipants() {
            konkursParticipants = new ArrayList<>();
        }

        @Override
        public String getBotUsername() { return BOT_USERNAME; }

        @Override
        public String getBotToken() { return BOT_TOKEN; }

        @Override
        public void onUpdateReceived(Update update) {
            try {
                if (update.hasMessage() && update.getMessage().getFrom() != null) {
                    long userId = update.getMessage().getFrom().getId();

                    if (!botEnabled && !ADMIN_IDS.contains(userId)) {
                        sendText(userId, "❌ Bot hozirda ishlamayapti. Iltimos, keyinroq urinib ko'ring.");
                        return;
                    }

                    if (bannedUsers.contains(userId)) {
                        sendText(userId, "❌ Siz botdan foydalanish huquqidan mahrum qilingansiz!");
                        return;
                    }
                }

                if (update.hasChannelPost()) {
                    handleChannelPost(update.getChannelPost());
                    return;
                }

                if (update.hasMessage() && isGroupOrChannel(update.getMessage().getChatId())) {
                    handleGroupMessage(update.getMessage());
                    return;
                }

                if (update.hasMessage()) {
                    if (update.getMessage().getFrom() != null) {
                        long userId = update.getMessage().getFrom().getId();
                        String username = update.getMessage().getFrom().getUserName();
                        if (username != null) {
                            userUsernameMap.put(userId, "@" + username);
                        }
                    }
                    handleMessage(update.getMessage());
                } else if (update.hasCallbackQuery()) {
                    handleCallback(update.getCallbackQuery());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(Message msg) throws Exception {
            long chatId = msg.getChatId();
            String state = stateMap.getOrDefault(chatId, "");
            String mediaType = mediaTypeMap.getOrDefault(chatId, "");

            System.out.println("DEBUG: handleMessage chatId=" + chatId + ", state=" + state + ", text=" + (msg.hasText() ? msg.getText() : "no text"));

            if (msg.hasText() && msg.getText().startsWith("/start")) {
                String text = msg.getText();
                if (text.contains(" ")) {
                    String[] parts = text.split(" ");
                    if (parts.length > 1) {
                        String referralCode = parts[1];
                        if (codeToUserMap.containsKey(referralCode)) {
                            Long referrerId = codeToUserMap.get(referralCode);
                            // Referralni saqlab qo'yamiz, to'liq ro'yxatdan o'tgandan keyin ball beramiz
                            referralMap.put(chatId, referrerId);
                            sendText(chatId, "🎉 Siz referral orqali kirdingiz! Ro'yxatdan o'tgandan keyin ball olasiz.");
                        }
                    }
                }

                if (userRegisteredMap.getOrDefault(chatId, false)) {
                    sendThreeButtonMenu(chatId);
                    return;
                }

                sendPhoneRequest(chatId);
                stateMap.put(chatId, "awaiting_phone");
                return;
            }

            if ("awaiting_phone".equals(state)) {
                if (msg.hasContact()) {
                    Contact contact = msg.getContact();
                    String phoneNumber = contact.getPhoneNumber();

                    if (!phoneNumber.startsWith("+")) {
                        phoneNumber = "+" + phoneNumber;
                    }

                    userPhones.put(chatId, phoneNumber);
                    phoneMap.put(chatId, phoneNumber);

                    String firstName = msg.getFrom().getFirstName() != null ? msg.getFrom().getFirstName() : "";
                    String lastName = msg.getFrom().getLastName() != null ? " " + msg.getFrom().getLastName() : "";
                    String fullName = (firstName + lastName).trim();
                    if (fullName.isEmpty()) {
                        fullName = "Foydalanuvchi";
                    }
                    userFullNameMap.put(chatId, fullName);

                    completeRegistration(chatId);
                    sendUserInfoToTechnicalAdmin(chatId);
                    sendThreeButtonMenu(chatId);

                    stateMap.put(chatId, "");
                    return;
                } else if (msg.hasText()) {
                    String phoneText = msg.getText().trim();
                    if (isValidPhoneNumber(phoneText)) {
                        userPhones.put(chatId, phoneText);
                        phoneMap.put(chatId, phoneText);

                        String firstName = msg.getFrom().getFirstName() != null ? msg.getFrom().getFirstName() : "";
                        String lastName = msg.getFrom().getLastName() != null ? " " + msg.getFrom().getLastName() : "";
                        String fullName = (firstName + lastName).trim();
                        if (fullName.isEmpty()) {
                            fullName = "Foydalanuvchi";
                        }
                        userFullNameMap.put(chatId, fullName);

                        completeRegistration(chatId);
                        sendUserInfoToTechnicalAdmin(chatId);
                        sendThreeButtonMenu(chatId);

                        stateMap.put(chatId, "");
                        return;
                    } else {
                        sendText(chatId, "❌ Iltimos, to'g'ri telefon raqam kiriting yoki \"📞 Raqamni yuborish\" tugmasini bosing.");
                        sendPhoneRequest(chatId);
                        return;
                    }
                }
            }

            // ADMINLAR UCHUN
            if (ADMIN_IDS.contains(chatId)) {
                // 1. Foydalanuvchi boshqarish uchun
                if ("tech_block_user".equals(state)) {
                    if (msg.hasText()) {
                        String text = msg.getText().trim();
                        if (text.equals("/cancel")) {
                            sendText(chatId, "❌ Bloklash bekor qilindi.");
                            sendTechAdminUserManagement(chatId);
                            stateMap.put(chatId, "");
                            return;
                        }

                        try {
                            int userNumber = Integer.parseInt(text);
                            banUser(chatId, userNumber);
                            sendTechAdminUserManagement(chatId);
                            stateMap.put(chatId, "");
                        } catch (NumberFormatException e) {
                            sendText(chatId, "❌ Iltimos, raqam kiriting! Masalan: 1, 2, 3");
                        }
                    }
                    return;
                }

                if ("tech_unblock_user_select".equals(state)) {
                    if (msg.hasText()) {
                        try {
                            int selection = Integer.parseInt(msg.getText().trim());
                            unblockUser(chatId, selection);
                            sendTechAdminUserManagement(chatId);
                            stateMap.put(chatId, "");
                        } catch (NumberFormatException e) {
                            sendText(chatId, "❌ Iltimos, raqam kiriting!");
                        }
                    }
                    return;
                }

                if ("tech_search_user".equals(state)) {
                    if (msg.hasText()) {
                        searchUserByNumber(chatId, msg.getText().trim());
                        sendTechAdminUserManagement(chatId);
                        stateMap.put(chatId, "");
                    }
                    return;
                }

                if ("tech_search_referral".equals(state)) {
                    if (msg.hasText()) {
                        String text = msg.getText().trim();
                        try {
                            int userId = Integer.parseInt(text);
                            searchReferralsById(chatId, userId);
                            stateMap.put(chatId, "");
                        } catch (NumberFormatException e) {
                            sendText(chatId, "❌ Iltimos, foydalanuvchi raqamini kiriting!");
                        }
                    }
                    return;
                }

                if ("admin_await_konkurs_image_only".equals(state)) {
                    if (msg.hasPhoto()) {
                        List<PhotoSize> photos = msg.getPhoto();
                        String fileId = photos.get(photos.size()-1).getFileId();
                        String newImageUrl = getFileUrl(fileId);
                        currentKonkursImageUrl = newImageUrl;
                        sendText(chatId, "✅ Konkurs rasmi muvaffaqiyatli yangilandi!");
                        stateMap.put(chatId, "");
                        sendKonkursMukofot(chatId);
                    } else {
                        sendText(chatId, "❌ Iltimos, faqat rasm yuboring!");
                    }
                    return;
                }

                if ("admin_await_konkurs_text_only".equals(state)) {
                    if (msg.hasText()) {
                        currentKonkursText = msg.getText();
                        sendText(chatId, "✅ Konkurs matni muvaffaqiyatli yangilandi!");
                        stateMap.put(chatId, "");
                        sendKonkursMukofot(chatId);
                    } else {
                        sendText(chatId, "❌ Iltimos, faqat matn yuboring!");
                    }
                    return;
                }

                if ("admin_await_konkurs_image_both".equals(state)) {
                    if (msg.hasPhoto()) {
                        List<PhotoSize> photos = msg.getPhoto();
                        String fileId = photos.get(photos.size()-1).getFileId();
                        String newImageUrl = getFileUrl(fileId);
                        currentKonkursImageUrl = newImageUrl;
                        stateMap.put(chatId, "admin_await_konkurs_text_both");
                        sendText(chatId, "✅ Rasm qabul qilindi! Endi yangi konkurs matnini yuboring:");
                    } else {
                        sendText(chatId, "❌ Iltimos, faqat rasm yuboring!");
                    }
                    return;
                }

                if ("admin_await_konkurs_text_both".equals(state)) {
                    if (msg.hasText()) {
                        currentKonkursText = msg.getText();
                        sendText(chatId, "✅ Konkurs rasmi va matni muvaffaqiyatli yangilandi!");
                        stateMap.put(chatId, "");
                        sendKonkursMukofot(chatId);
                    } else {
                        sendText(chatId, "❌ Iltimos, faqat matn yuboring!");
                    }
                    return;
                }

                if (state.startsWith("admin_decline_reason_")) {
                    String userIdStr = state.substring("admin_decline_reason_".length());
                    long userId = Long.parseLong(userIdStr);
                    String reason = msg.getText();
                    declineReasonsMap.put(userId, reason);
                    sendText(userId, "❌ E'loningiz tasdiqlanmadi!\n\n📝 Sabab: " + reason);
                    sendText(chatId, "✅ Foydalanuvchiga rad etish sababi yuborildi.");
                    stateMap.put(chatId, "");
                    return;
                }

                if (state.startsWith("admin_edit_")) {
                    String editType = state.substring("admin_edit_".length());
                    Long userId = adminEditUserIdMap.get(chatId);

                    if (userId != null) {
                        String newValue = msg.getText().trim();
                        switch (editType) {
                            case "manzil":
                                manzilMap.put(userId, newValue);
                                sendText(chatId, "✅ Manzil o'zgartirildi: " + newValue);
                                break;
                            case "phone":
                                phoneMap.put(userId, newValue);
                                sendText(chatId, "✅ Telefon raqami o'zgartirildi: " + newValue);
                                break;
                            case "price":
                                priceMap.put(userId, newValue);
                                sendText(chatId, "✅ Narx o'zgartirildi: " + newValue);
                                break;
                        }
                        sendAdminEditMenu(chatId, userId);
                        stateMap.put(chatId, "");
                    }
                    return;
                }

                if (chatId == TECHNICAL_ADMIN_ID) {
                    if ("ad_await_text".equals(state)) {
                        if (msg.hasText() && msg.getText().length() <= 1000) {
                            adTextMap.put(chatId, msg.getText());
                            askForAdPhoto(chatId);
                        } else {
                            sendText(chatId, "❌ Matn 1000 belgidan oshmasligi kerak yoki matn bo'lishi shart!");
                        }
                        return;
                    }

                    if ("ad_await_photo".equals(state)) {
                        if (msg.hasPhoto()) {
                            List<PhotoSize> photos = msg.getPhoto();
                            String fileId = photos.get(photos.size()-1).getFileId();
                            adPhotoMap.put(chatId, fileId);
                            showAdPreview(chatId);
                        } else {
                            sendText(chatId, "❌ Iltimos, rasm yuboring yoki \"Rasmsiz yuborish\" tugmasini bosing!");
                        }
                        return;
                    }
                }
            }

            // Foydalanuvchi tomonidan yozilgan zot
            if ("await_custom_breed".equals(state)) {
                breedMap.put(chatId, msg.getText().trim());
                sendAgeSelection(chatId);
                return;
            }

            // Narx kiritish
            if ("await_price".equals(state)) {
                priceMap.put(chatId, msg.getText().trim());
                stateMap.put(chatId, "await_phone");
                sendText(chatId, "💰 Narx: " + msg.getText().trim() +
                        ("so'm".equals(valyutaMap.getOrDefault(chatId, "so'm")) ? " so'm" : " $") + "\n\n" +
                        "📍 Manzil: " + manzilMap.getOrDefault(chatId, "—") +
                        "\n📞 Endi telefon raqamingizni yuboring: (masalan +998 90 123 45 67)");
                return;
            }

            // To'lov cheki
            if ("wait_check".equals(state)) {
                if (msg.hasPhoto()) {
                    List<PhotoSize> photos = msg.getPhoto();
                    String fileId = photos.get(photos.size()-1).getFileId();
                    checkMap.put(chatId, fileId);
                    sendText(chatId, "✅ Chek qabul qilindi. Admin tekshiradi.");

                    lastAdTimeMap.put(chatId, new Date());
                    userHasPendingAdMap.put(chatId, true);
                    notifyAdmin(chatId);
                    stateMap.put(chatId, "waiting_admin");
                } else {
                    sendText(chatId, "❌ Iltimos, to'lov chekining rasmini yuboring.");
                }
                return;
            }

            // Matn xabarlarini qayta ishlash
            if (msg.hasText()) {
                String text = msg.getText().trim();

                if ("yordam_await_info".equals(state)) {
                    String userInfo = text;
                    manzilMap.put(chatId, userInfo);
                    sendYordamPreview(chatId, userInfo);
                    return;
                }

                if ("await_phone".equals(state)) {
                    if (!isValidPhoneNumber(text)) {
                        sendText(chatId, "❌ Iltimos, telefon raqamni to'g'ri formatda kiriting:\n\n+998 ** *** ** **\n\nMasalan: +998 90 123 45 67\n\nQayta urinib ko'ring:");
                        return;
                    }
                    phoneMap.put(chatId, text);

                    if ("hadiya".equals(adTypeMap.get(chatId))) {
                        sendPreview(chatId);
                    } else {
                        sendBreedSelectionWithCustom(chatId, 1);
                    }
                    return;
                }

                if ("await_feedback".equals(state)) {
                    sendFeedbackToOwner(chatId, text);
                    sendText(chatId, "✅ Sizning fikringiz adminlarga yuborildi. Rahmat!");
                    sendMainMenu(chatId);
                    stateMap.put(chatId, "");
                    return;
                }

                sendText(chatId, "Iltimos, tugmalardan foydalaning yoki /start ni bosing.");
                return;
            }

            // RASM QABUL QILISH
            if (msg.hasPhoto() && "await_photo".equals(state) && "photo".equals(mediaType)) {
                List<PhotoSize> photos = msg.getPhoto();
                if (photos == null || photos.isEmpty()) {
                    sendText(chatId, "❌ Rasm topilmadi. Qayta yuboring.");
                    return;
                }

                String fileId = photos.get(photos.size()-1).getFileId();
                System.out.println("DEBUG: Rasm qabul qilindi. File ID: " + fileId);

                if (!photosMap.containsKey(chatId)) {
                    photosMap.put(chatId, new ArrayList<>());
                }

                List<String> userPhotos = photosMap.get(chatId);
                if (userPhotos.size() < 3) {
                    userPhotos.add(fileId);

                    sendText(chatId, "✅ " + userPhotos.size() + "-rasm qabul qilindi!");

                    if (userPhotos.size() >= 3) {
                        sendText(chatId, "📸 Maksimum 3 ta rasm yubordingiz. Davom etish tugmasini bosing.");
                        sendContinueButton(chatId);
                    } else {
                        sendContinueButton(chatId);
                    }
                } else {
                    sendText(chatId, "❌ Siz allaqachon 3 ta rasm yuborgansiz. Davom etish tugmasini bosing!");
                    sendContinueButton(chatId);
                }
                return;
            }

            // VIDEO QABUL QILISH
            if (msg.hasVideo() && "await_photo".equals(state) && "video".equals(mediaType)) {
                Video video = msg.getVideo();

                if (video.getDuration() <= 10) {
                    String fileId = video.getFileId();

                    if (!photosMap.containsKey(chatId)) {
                        photosMap.put(chatId, new ArrayList<>());
                    }

                    List<String> userPhotos = photosMap.get(chatId);
                    if (userPhotos.isEmpty()) {
                        userPhotos.add("video:" + fileId);
                        sendText(chatId, "✅ Video qabul qilindi! (10 soniyagacha)\n\n" +
                                "Endi 'Davom etish' tugmasini bosing.");
                        sendContinueButton(chatId);
                    } else {
                        sendText(chatId, "❌ Siz allaqachon video yuborgansiz. Faqat 1 ta video yuborishingiz mumkin.");
                    }

                } else {
                    sendText(chatId, "❌ Video 10 soniyadan uzun! Iltimos, 10 soniyagacha bo'lgan video yuboring.");
                }
                return;
            }

            // Yordam uchun rasm qabul qilish
            if (msg.hasPhoto() && state.startsWith("yordam_") && state.endsWith("_photo")) {
                List<PhotoSize> photos = msg.getPhoto();
                String fileId = photos.get(photos.size()-1).getFileId();

                if (!photosMap.containsKey(chatId)) {
                    photosMap.put(chatId, new ArrayList<>());
                }

                List<String> userPhotos = photosMap.get(chatId);
                if (userPhotos.size() < 3) {
                    userPhotos.add(fileId);

                    int currentCount = userPhotos.size();
                    if (currentCount >= 3) {
                        sendText(chatId, "✅ 3-rasm qabul qilindi. Maksimum 3 ta rasm yuborishingiz mumkin.");
                        sendYordamViloyatSelection(chatId);
                    } else {
                        sendText(chatId, "✅ " + currentCount + "-rasm qabul qilindi. " +
                                (3 - currentCount) + " ta rasm yuborishingiz mumkin yoki 'Davom etish' tugmasini bosing.");
                    }
                } else {
                    sendText(chatId, "❌ Maksimum 3 ta rasm yuborishingiz mumkin. 'Davom etish' tugmasini bosing.");
                }
                return;
            }

            // Yordam uchun video qabul qilish
            if (msg.hasVideo() && state.startsWith("yordam_") && state.endsWith("_photo")) {
                Video video = msg.getVideo();

                if (video.getDuration() <= 10) {
                    String fileId = video.getFileId();

                    if (!photosMap.containsKey(chatId)) {
                        photosMap.put(chatId, new ArrayList<>());
                    }

                    List<String> userPhotos = photosMap.get(chatId);
                    userPhotos.add("video:" + fileId);

                    sendText(chatId, "✅ Video qabul qilindi! (10 soniyagacha)\n\n" +
                            "Endi 'Davom etish' tugmasini bosing.");

                } else {
                    sendText(chatId, "❌ Video 10 soniyadan uzun! Iltimos, 10 soniyagacha bo'lgan video yuboring.");
                }
                return;
            }

            sendText(chatId, "Iltimos, tugmalardan foydalaning yoki /start ni bosing.");
        }

        private void handleCallback(CallbackQuery cb) throws Exception {
            long chatId = cb.getMessage().getChatId();
            String data = cb.getData();
            long fromId = cb.getFrom().getId();

            execute(new AnswerCallbackQuery(cb.getId()));
            System.out.println("Callback received: " + data + " from: " + chatId);

            // E'lonlarni ko'rish va boshqarish
            if (data.startsWith("view_ad_")) {
                long adId = Long.parseLong(data.substring("view_ad_".length()));
                showIndividualAd(chatId, adId);
                return;
            }

            if (data.startsWith("ads_page_")) {
                int page = Integer.parseInt(data.substring("ads_page_".length()));
                showUserAdsInPagesWithPage(chatId, page);
                return;
            }

            if (data.equals("back_to_ads_list")) {
                showUserAdsInPages(chatId);
                return;
            }

            if (data.startsWith("mark_completed_")) {
                long adId = Long.parseLong(data.substring("mark_completed_".length()));
                markAdAsCompleted(chatId, adId);
                return;
            }

            if (data.startsWith("mark_sold_")) {
                long adId = Long.parseLong(data.substring("mark_sold_".length()));
                markAdAsSold(chatId, adId);
                return;
            }

            if (data.startsWith("view_in_channel_")) {
                String adIdStr = data.substring("view_in_channel_".length());
                long adId = Long.parseLong(adIdStr);
                showChannelAdLink(chatId, adId);
                return;
            }

            // Asosiy menyu navigatsiyasi
            if (data.equals("menu_main")) {
                sendMainMenu(chatId);
                return;
            }

            if (data.equals("menu_back_to_three")) {
                sendThreeButtonMenu(chatId);
                return;
            }

            if (data.equals("menu_reklama")) {
                if (userHasPendingAdMap.getOrDefault(chatId, false)) {
                    Date lastAdTime = lastAdTimeMap.get(chatId);
                    if (lastAdTime != null) {
                        long hoursPassed = (new Date().getTime() - lastAdTime.getTime()) / (60 * 60 * 1000);
                        if (hoursPassed < 24) {
                            sendText(chatId, "⏳ Hozirda sizda tasdiqlanmagan reklama mavjud!\n\n" +
                                    "Agar admin 24 soat ichida tasdiqlamasa, yangi reklama berishingiz mumkin bo'ladi.");
                            return;
                        }
                    }
                }
                sendAdTypeSelection(chatId);
                return;
            }

            if (data.equals("menu_konkurs")) {
                sendKonkursMenu(chatId);
                return;
            }

            if (data.equals("menu_my_orders")) {
                showUserAdsInPages(chatId);
                return;
            }

            if (data.equals("menu_about")) {
                sendAboutMenu(chatId);
                return;
            }

            if (data.equals("about_back")) {
                sendMainMenu(chatId);
                return;
            }

            if (data.equals("about_send_feedback")) {
                stateMap.put(chatId, "await_feedback");
                sendText(chatId, "✍️ Iltimos, shikoyat yoki takliflaringizni yozib qoldiring:");
                return;
            }

            // Admin bilan bog'lanish
            if (data.equals("menu_admin")) {
                sendAdminContactMenu(chatId);
                return;
            }

            if (data.equals("contact_channel_admin")) {
                sendText(chatId, "👤 *Kanal egasi va Bot admini bilan bog'lanish:*\n\n" +
                        "Assalomu alaykum Uzbek Cats botiga hush kelibsiz! " +
                        "Siz agar kanal egasi va botni admini bilan bog'lanmoqchi bo'lsangiz shu " +
                        "numerlarga aloqaga chinging:\n\n" +
                        "📱 Telegram: @zayd_catlover\n" +
                        "📞 Telefon: +998934938181");
                return;
            }

            if (data.equals("contact_tech_admin")) {
                sendText(chatId, "👨‍💻 *Teknik Admin bilan bog'lanish:*\n\n" +
                        "Assalomu alaykum Uzbek Cats botiga hush kelibsiz! " +
                        "Agar siz botimiz uchun taklif yoki shikoyat yoki biror joyini tushunmagan bo'lsangiz " +
                        "Texnik Admin bilan bog'laning:\n\n" +
                        "📱 Telegram: @It_project_2026\n" +
                        "📞 Telefon: +998** *** ** **");
                return;
            }

            if (data.equals("vyazka_diniy_confirm")) {
                stateMap.put(chatId, "await_phone");
                sendText(chatId, "📞 Endi telefon raqamingizni yuboring: (masalan +998 90 123 45 67)");
                return;
            }

            // Admin panel navigatsiyasi
            if (data.equals("admin_panel")) {
                sendAdminPanel(chatId);
                return;
            }

            if (data.equals("tech_admin_panel")) {
                sendTechnicalAdminMenu(chatId);
                return;
            }

            // Teknik admin foydalanuvchi boshqarish
            if (data.equals("tech_user_management")) {
                sendTechAdminUserManagement(chatId);
                return;
            }

            if (data.equals("tech_block_user")) {
                if (ADMIN_IDS.contains(fromId)) {
                    stateMap.put(chatId, "tech_block_user");
                    sendText(chatId, "⛔ Foydalanuvchini bloklash uchun uning raqamini kiriting:\n\n" +
                            "Masalan: 1, 2, 3\n\n" +
                            "Yoki /cancel ni bosing bekor qilish uchun.");
                }
                return;
            }

            if (data.equals("tech_unblock_user")) {
                if (ADMIN_IDS.contains(fromId)) {
                    showBlockedUsers(chatId);
                }
                return;
            }

            if (data.equals("tech_search_user")) {
                if (ADMIN_IDS.contains(fromId)) {
                    stateMap.put(chatId, "tech_search_user");
                    sendText(chatId, "🔍 Foydalanuvchi izlash uchun uning raqamini kiriting:\n\n" +
                            "Masalan: 1, 2, 3\n\n" +
                            "Yoki /cancel ni bosing bekor qilish uchun.");
                }
                return;
            }

            if (data.equals("tech_search_referral")) {
                if (ADMIN_IDS.contains(fromId)) {
                    stateMap.put(chatId, "tech_search_referral");
                    sendText(chatId, "🔍 Taklif qilgan foydalanuvchi raqamini kiriting:\n\n" +
                            "Masalan: 1, 2, 3\n\n" +
                            "Yoki /cancel ni bosing bekor qilish uchun.");
                }
                return;
            }

            // Konkurs boshqarish
            if (data.equals("admin_konkurs_change")) {
                if (ADMIN_IDS.contains(fromId)) {
                    sendKonkursChangeMenu(chatId);
                }
                return;
            }

            if (data.equals("admin_konkurs_image_only")) {
                if (ADMIN_IDS.contains(fromId)) {
                    handleAdminKonkursImageOnly(chatId);
                }
                return;
            }

            if (data.equals("admin_konkurs_text_only")) {
                if (ADMIN_IDS.contains(fromId)) {
                    handleAdminKonkursTextOnly(chatId);
                }
                return;
            }

            if (data.equals("admin_konkurs_both")) {
                if (ADMIN_IDS.contains(fromId)) {
                    handleAdminKonkursBoth(chatId);
                }
                return;
            }

            // Reklama paneli
            if (data.equals("ad_panel")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    sendAdPanel(chatId);
                }
                return;
            }

            if (data.equals("ad_new")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    startNewAd(chatId);
                }
                return;
            }

            if (data.equals("ad_no_photo")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    adPhotoMap.put(chatId, "");
                    showAdPreview(chatId);
                }
                return;
            }

            if (data.equals("ad_send")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    broadcastAd(chatId);
                }
                return;
            }

            if (data.equals("ad_edit")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    startNewAd(chatId);
                }
                return;
            }

            if (data.equals("ad_cancel")) {
                if (fromId == TECHNICAL_ADMIN_ID) {
                    adTextMap.remove(chatId);
                    adPhotoMap.remove(chatId);
                    stateMap.put(chatId, "");
                    sendText(chatId, "❌ Reklama bekor qilindi.");
                    sendAdPanel(chatId);
                }
                return;
            }

            if (data.equals("ad_back")) {
                sendThreeButtonMenu(chatId);
                return;
            }

            // Media turini tanlash
            if (data.equals("media_photo")) {
                mediaTypeMap.put(chatId, "photo");
                stateMap.put(chatId, "await_photo");
                photosMap.put(chatId, new ArrayList<>());

                String adType = adTypeMap.getOrDefault(chatId, "");

                if ("vyazka".equals(adType)) {
                    sendPlatformaSelection(chatId);
                } else {
                    String instruction = "🖼️ Iltimos, mushukning rasmlarini yuboring:\n\n" +
                            "• 1 dan 3 tagacha bo'lgan surat jo'natishingiz mumkin\n" +
                            "• Rasmlar aniq va yorug' bo'lsin";
                    sendText(chatId, instruction);
                }
                return;
            }

            if (data.equals("media_video")) {
                mediaTypeMap.put(chatId, "video");
                stateMap.put(chatId, "await_photo");
                photosMap.put(chatId, new ArrayList<>());

                String adType = adTypeMap.getOrDefault(chatId, "");

                if ("vyazka".equals(adType)) {
                    sendPlatformaSelection(chatId);
                } else {
                    String instruction = "🎥 Iltimos, mushukning videosini yuboring:\n\n" +
                            "• Video 10 soniyadan uzun bo'lmasin\n" +
                            "• Video aniq va yorug' bo'lsin";
                    sendText(chatId, instruction);
                }
                return;
            }

            // Breed tanlash
            if (data.startsWith("admin_set_breed_")) {
                handleAdminSetBreed(chatId, data);
                return;
            }

            if (data.startsWith("admin_edit_field_")) {
                handleAdminEditField(chatId, data);
                return;
            }

            if (data.startsWith("breed_page_")) {
                int page = Integer.parseInt(data.substring("breed_page_".length()));
                sendBreedSelectionWithCustom(chatId, page);
                return;
            }

            if (data.startsWith("breed_select_")) {
                String breedData = data.substring("breed_select_".length());
                String[] parts = breedData.split("_", 2);
                int page = Integer.parseInt(parts[0]);
                int breedIndex = Integer.parseInt(parts[1]);

                String selectedBreed = breedPages.get(page).get(breedIndex);
                breedMap.put(chatId, selectedBreed);
                sendAgeSelection(chatId);
                return;
            }

            // Platforma tanlash
            if (data.startsWith("platforma_")) {
                String platforma = data.substring("platforma_".length());
                platformaMap.put(chatId, platforma);

                if ("instagram".equals(platforma)) {
                    sendText(chatId, "📞 Iltimos Admin bilan bog'laning:\n\n" +
                            "👤 Admin: @zayd_catlover\n" +
                            "📞 Telefon: +998934938181");
                    stateMap.put(chatId, "");
                    return;
                }

                String mediaType = mediaTypeMap.getOrDefault(chatId, "photo");
                if ("video".equals(mediaType)) {
                    sendText(chatId, "🎥 Iltimos, mushukning videosini yuboring:\n\n" +
                            " • Mushukchani chiroyli suratidan jo'nating \n" +
                            " • Video 10 soniyadan uzun bo'lmasin\n" +
                            " • Video aniq va yorug' bo'lsin");
                } else {
                    sendText(chatId, "📸 Iltimos, mushukning rasmlarini yuboring:\n\n" +
                            " • Mushukchani chiroyli suratidan jo'nating \n" +
                            " • 1 dan 3 tagacha bo'lgan surat jo'natishingiz mumkin\n" +
                            " • yoki 5-10 sekundgacha video jo'ylashingiz mumkin 10 sekuntdan\n\n" +
                            " • ortiq videoni qabul qilmaymiz ❗️\uFE0F");
                }
                return;
            }

            // Valyuta tanlash
            if (data.equals("valyuta_som") || data.equals("valyuta_dollar")) {
                String valyuta = data.equals("valyuta_som") ? "so'm" : "$";
                valyutaMap.put(chatId, valyuta);

                stateMap.put(chatId, "await_price");
                sendText(chatId, "💰 Mushukchangizni nech " + valyuta + "ga " +
                        ("sotish".equals(adTypeMap.get(chatId)) ? "sotmoqchisiz?" : "vyazkaga qo'moqchisiz?") +
                        "\n\nEslatma: Bozor narxlarni hisobga olgan holda, mushugingizga mos narx qo'ying.\n" +
                        "Masalan: " + ("so'm".equals(valyuta) ? "100.000" : "100"));
                return;
            }

            // Asosiy callback'lar
            switch (data) {
                case "menu_narx":
                    sendPriceList(chatId);
                    break;

                case "menu_yordam":
                    sendYordamMenu(chatId);
                    break;

                case "konkurs_mukofot":
                    sendKonkursMukofot(chatId);
                    break;

                case "konkurs_reting":
                    sendKonkursRating(chatId);
                    break;

                case "konkurs_shartlar":
                    sendKonkursShartlar(chatId);
                    break;

                case "konkurs_back":
                    sendThreeButtonMenu(chatId);
                    break;

                case "admin_stats":
                    sendAdminStatisticsMenu(chatId);
                    break;

                case "admin_bot_control":
                    sendBotControlMenu(chatId);
                    break;

                case "admin_toggle_bot":
                    botEnabled = !botEnabled;
                    sendText(chatId, botEnabled ? "✅ Bot yoqildi!" : "🔴 Bot o'chirildi!");
                    sendBotControlMenu(chatId);
                    break;

                case "admin_bot_stats":
                    sendBotStatistics(chatId);
                    break;

                case "admin_ban_user":
                    if (ADMIN_IDS.contains(fromId)) {
                        stateMap.put(chatId, "admin_await_ban_user");
                        sendText(chatId, "⛔ Foydalanuvchini bloklash uchun uning raqamini kiriting:\n\n" +
                                "Masalan: 1, 2, 3\n\n" +
                                "Yoki /cancel ni bosing bekor qilish uchun.");
                    }
                    break;

                case "stat_hadiya":
                    showStatistics(chatId, "hadiya");
                    break;

                case "stat_sotish":
                    showStatistics(chatId, "sotish");
                    break;

                case "stat_vyazka":
                    showStatistics(chatId, "vyazka");
                    break;

                case "stat_back":
                    sendAdminStatisticsMenu(chatId);
                    break;

                case "admin_back":
                    sendAdminPanel(chatId);
                    break;

                case "admin_rating_manage":
                    if (ADMIN_IDS.contains(fromId)) {
                        sendAdminRatingManagement(chatId);
                    }
                    break;

                case "rating_reset_all":
                    if (ADMIN_IDS.contains(fromId)) {
                        handleResetAllRatings(chatId);
                    }
                    break;

                case "about_back":
                    sendMainMenu(chatId);
                    break;

                case "about_what_can":
                    sendText(chatId, "Ushbu bot orqali siz mushuklar haqida e'lon berishingiz mumkin.");
                    sendMainMenu(chatId);
                    break;

                case "about_need_bot":
                    sendText(chatId, "Taklif va shikoyatlar va sizga ham bot kerak bo'lsa shu raqamgaga aloqaga chiqing: +998900512621");
                    sendMainMenu(chatId);
                    break;

                case "adtype_sotish":
                    adTypeMap.put(chatId, "sotish");
                    sendMushukSoniSelection(chatId);
                    break;

                case "adtype_hadiya":
                    adTypeMap.put(chatId, "hadiya");
                    sendMediaTypeSelection(chatId);
                    break;

                case "adtype_vyazka":
                    adTypeMap.put(chatId, "vyazka");
                    sendMediaTypeSelection(chatId);
                    break;

                case "adtype_back":
                    sendThreeButtonMenu(chatId);
                    break;

                case "continue_process":
                    handleContinueProcess(chatId);
                    break;

                case "mushuk_1":
                    handleMushukSoni(chatId, 1);
                    break;

                case "mushuk_2":
                    handleMushukSoni(chatId, 2);
                    break;

                case "mushuk_3":
                    handleMushukSoni(chatId, 3);
                    break;

                case "mushuk_4":
                    handleMushukSoni(chatId, 4);
                    break;

                case "mushuk_5":
                    handleMushukSoni(chatId, 5);
                    break;

                case "mushuk_kop":
                    handleMushukSoni(chatId, 6);
                    break;

                case "breed_custom":
                    stateMap.put(chatId, "await_custom_breed");
                    sendText(chatId, "✏️ Iltimos, mushukingiz zotini yozing:");
                    break;

                case "yordam_back":
                    sendMainMenu(chatId);
                    break;

                case "yordam_onasiz":
                    handleYordamOnasiz(chatId);
                    break;

                case "yordam_kasal":
                    handleYordamKasal(chatId);
                    break;

                case "yordam_kasal_hadiya":
                    handleYordamKasalHadiya(chatId);
                    break;

                case "yordam_confirm":
                    handleYordamConfirm(chatId);
                    break;

                case "yordam_cancel":
                    sendYordamMenu(chatId);
                    break;

                case "yordam_final_confirm":
                    String yordamType = stateMap.get(chatId);
                    notifyAdminForYordam(chatId, yordamType);
                    sendText(chatId, "✅ So'rovingiz qabul qilindi! Admin tekshiradi.");
                    stateMap.put(chatId, "waiting_admin");
                    break;

                case "viloyat_andijon": case "viloyat_buxoro": case "viloyat_fargona":
                case "viloyat_jizzax": case "viloyat_xorazm": case "viloyat_namangan":
                case "viloyat_navoiy": case "viloyat_qashqadaryo": case "viloyat_samarqand":
                case "viloyat_sirdaryo": case "viloyat_surxondaryo": case "viloyat_toshkent":
                case "viloyat_toshkent_shahar":
                    String viloyat = data.replace("viloyat_", "").replace("_", " ");
                    manzilMap.put(chatId, viloyat);

                    String adType = adTypeMap.getOrDefault(chatId, "");
                    if ("sotish".equals(adType)) {
                        sendValyutaSelection(chatId);
                    } else if ("vyazka".equals(adType)) {
                        sendViloyatSelectionForVyazka(chatId, viloyat);
                    } else {
                        stateMap.put(chatId, "await_phone");
                        sendText(chatId, "📍 Manzil: " + viloyat + "\n📞 Endi telefon raqamingizni yuboring: (masalan +998 90 123 45 67)");
                    }
                    break;

                case "yordam_viloyat_andijon": case "yordam_viloyat_buxoro": case "yordam_viloyat_fargona":
                case "yordam_viloyat_jizzax": case "yordam_viloyat_xorazm": case "yordam_viloyat_namangan":
                case "yordam_viloyat_navoiy": case "yordam_viloyat_qashqadaryo": case "yordam_viloyat_samarqand":
                case "yordam_viloyat_sirdaryo": case "yordam_viloyat_surxondaryo": case "yordam_viloyat_toshkent":
                case "yordam_viloyat_toshkent_shahar":
                    String yordamViloyat = data.replace("yordam_viloyat_", "").replace("_", " ");
                    manzilMap.put(chatId, yordamViloyat);
                    stateMap.put(chatId, "yordam_await_phone");
                    sendText(chatId, "📍 Manzil: " + yordamViloyat + "\n📞 Endi telefon raqamingizni yuboring: (masalan +998 90 123 45 67)");
                    break;

                case "yordam_phone_confirm":
                    String currentState = stateMap.get(chatId);
                    if (currentState.startsWith("yordam_")) {
                        stateMap.put(chatId, currentState + "_photo");
                        sendText(chatId, "📸 Endi rasm yuboring (1-3 ta rasm yoki 10 soniyagacha video):");
                    }
                    break;

                // Yosh tanlash
                case "age_1_oylik": ageMap.put(chatId, "1 oylik"); sendHealthSelection(chatId); break;
                case "age_2_oylik": ageMap.put(chatId, "2 oylik"); sendHealthSelection(chatId); break;
                case "age_3_oylik": ageMap.put(chatId, "3 oylik"); sendHealthSelection(chatId); break;
                case "age_4_oylik": ageMap.put(chatId, "4 oylik"); sendHealthSelection(chatId); break;
                case "age_5_oylik": ageMap.put(chatId, "5 oylik"); sendHealthSelection(chatId); break;
                case "age_6_oylik": ageMap.put(chatId, "6 oylik"); sendHealthSelection(chatId); break;
                case "age_7_oylik": ageMap.put(chatId, "7 oylik"); sendHealthSelection(chatId); break;
                case "age_8_oylik": ageMap.put(chatId, "8 oylik"); sendHealthSelection(chatId); break;
                case "age_9_oylik": ageMap.put(chatId, "9 oylik"); sendHealthSelection(chatId); break;
                case "age_10_oylik": ageMap.put(chatId, "10 oylik"); sendHealthSelection(chatId); break;
                case "age_11_oylik": ageMap.put(chatId, "11 oylik"); sendHealthSelection(chatId); break;
                case "age_1_yosh": ageMap.put(chatId, "1 yosh"); sendHealthSelection(chatId); break;
                case "age_15_yosh": ageMap.put(chatId, "1,5 yosh"); sendHealthSelection(chatId); break;
                case "age_2_yosh": ageMap.put(chatId, "2 yosh"); sendHealthSelection(chatId); break;
                case "age_25_yosh": ageMap.put(chatId, "2,5 yosh"); sendHealthSelection(chatId); break;
                case "age_3_yosh": ageMap.put(chatId, "3 yosh"); sendHealthSelection(chatId); break;
                case "age_35_yosh": ageMap.put(chatId, "3,5 yosh"); sendHealthSelection(chatId); break;
                case "age_4_yosh": ageMap.put(chatId, "4 yosh"); sendHealthSelection(chatId); break;
                case "age_45_yosh": ageMap.put(chatId, "4,5 yosh"); sendHealthSelection(chatId); break;
                case "age_5_yosh": ageMap.put(chatId, "5 yosh"); sendHealthSelection(chatId); break;

                case "health_soglom":
                    healthMap.put(chatId, "Sog'lom");
                    sendGenderSelection(chatId);
                    break;

                case "health_kasal":
                    healthMap.put(chatId, "Kasal");
                    sendGenderSelection(chatId);
                    break;

                case "gender_qiz":
                    genderMap.put(chatId, "Qiz bola");
                    sendSterilizationSelection(chatId);
                    break;

                case "gender_ogil":
                    genderMap.put(chatId, "O'g'il bola");
                    sendSterilizationSelection(chatId);
                    break;

                case "sterilization_yes":
                    sterilizationMap.put(chatId, "Nasl olish mumkin");
                    sendPreview(chatId);
                    break;

                case "sterilization_no":
                    sterilizationMap.put(chatId, "Nasl olish mumkin emas");
                    sendPreview(chatId);
                    break;

                case "preview_confirm":
                    String currentAdType = adTypeMap.getOrDefault(chatId, "");

                    if ("sotish".equals(currentAdType) || "vyazka".equals(currentAdType)) {
                        sendPaymentInstructions(chatId);
                    } else {
                        sendText(chatId, "✅ Ma'lumotlaringiz qabul qilindi! Admin tekshirib kanalga joylaydi.");
                        userHasPendingAdMap.put(chatId, true);
                        notifyAdmin(chatId);
                        stateMap.put(chatId, "waiting_admin");
                    }
                    break;

                case "preview_back":
                    stateMap.put(chatId, "await_photo");
                    photosMap.remove(chatId);
                    sendText(chatId, "↩️ Orqaga qaytildi. Iltimos, rasmlarni qayta yuboring yoki /start ni bosing.");
                    break;

                case "admin_edit_breed":
                    handleAdminEditBreed(chatId);
                    break;

                case "admin_edit_confirm":
                    handleAdminEditConfirm(chatId);
                    break;

                case "admin_edit_cancel":
                    sendAdminPanel(chatId);
                    break;

                // Texnik admin uchun Konkurs takliflar
                case "tech_konkurs_takliflar":
                    sendKonkursTakliflar(chatId);
                    break;

                case "tech_konkurs_referrals":
                    sendKonkursReferralSelection(chatId);
                    break;

                case "tech_detailed_stats":
                    sendDetailedStats(chatId);
                    break;

                case "view_my_referrals":
                    showMyReferrals(chatId);
                    break;

                default:
                    if (data.startsWith("approve_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String uidStr = data.substring("approve_".length());
                            long uid = Long.parseLong(uidStr);

                            int adNumber = userAdCountMap.getOrDefault(uid, 0) + 1;
                            userAdCountMap.put(uid, adNumber);

                            int userNumber = userNumberMap.getOrDefault(uid, 0);
                            String username = userUsernameMap.getOrDefault(uid, "Noma'lum");
                            adType = adTypeMap.getOrDefault(uid, "");
                            String breed = breedMap.getOrDefault(uid, "");

                            String messageToTechAdmin = "📢 #" + userNumber + " FOYDALANUVCHI REKLAMA BERDI\n\n" +
                                    "👤 Foydalanuvchi: " + username + "\n" +
                                    "🔢 Reklama raqami: " + adNumber + "-reklama\n" +
                                    "📋 Reklama turi: " + adType + "\n" +
                                    "🐱 Mushuk zoti: " + breed + "\n" +
                                    "⏰ Vaqt: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

                            sendText(TECHNICAL_ADMIN_ID, messageToTechAdmin);

                            postToChannel(uid, adNumber);
                            sendText(uid, "✅ E'loningiz kanalga joylandi!");
                            deleteAdminMessages(uid);
                            sendText(fromId, "✅ E'lon tasdiqlandi va kanalga joylandi. Xabarlar tozalandi.");
                            userHasPendingAdMap.put(uid, false);
                        }
                    } else if (data.startsWith("decline_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String uidStr = data.substring("decline_".length());
                            long uid = Long.parseLong(uidStr);
                            stateMap.put(fromId, "admin_decline_reason_" + uid);
                            sendText(fromId, "📝 Foydalanuvchiga yuborish uchun rad etish sababini yozing:");
                            userHasPendingAdMap.put(uid, false);
                        }
                    } else if (data.startsWith("edit_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String uidStr = data.substring("edit_".length());
                            long uid = Long.parseLong(uidStr);
                            adminEditUserIdMap.put(fromId, uid);
                            sendAdminEditMenu(chatId, uid);
                        }
                    } else if (data.startsWith("yordam_approve_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String uidStr = data.substring("yordam_approve_".length());
                            long uid = Long.parseLong(uidStr);
                            postYordamToChannel(uid);
                            sendText(uid, "✅ So'rovingiz tasdiqlandi va kanalga joylandi!");
                            sendText(fromId, "✅ Yordam so'rovi tasdiqlandi.");
                        }
                    } else if (data.startsWith("yordam_decline_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String uidStr = data.substring("yordam_decline_".length());
                            long uid = Long.parseLong(uidStr);
                            sendText(uid, "❌ So'rovingiz tasdiqlanmadi. Admin bilan bog'laning.");
                            sendText(fromId, "❌ Yordam so'rovi rad etildi.");
                        }
                    } else if (data.startsWith("rating_reset_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            String userIdStr = data.substring("rating_reset_".length());
                            long userId = Long.parseLong(userIdStr);
                            userScores.put(userId, 0);
                            sendText(chatId, "✅ #" + userNumberMap.getOrDefault(userId, 0) + " foydalanuvchining ballari 0 ga tushirildi!");
                            sendAdminRatingManagement(chatId);
                        }
                    } else if (data.startsWith("referral_rank_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            int rank = Integer.parseInt(data.substring("referral_rank_".length()));
                            showReferralDetails(chatId, rank);
                        }
                        return;
                    } else if (data.startsWith("unblock_user_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            int index = Integer.parseInt(data.substring("unblock_user_".length()));
                            unblockUserByIndex(chatId, index);
                        }
                        return;
                    } else if (data.startsWith("tech_ref_details_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            int rank = Integer.parseInt(data.substring("tech_ref_details_".length()));
                            showReferralDetailsForAdmin(chatId, rank);
                        }
                        return;
                    } else if (data.startsWith("tech_reset_scores_")) {
                        if (ADMIN_IDS.contains(fromId)) {
                            Long userId = Long.parseLong(data.substring("tech_reset_scores_".length()));
                            userScores.put(userId, 0);

                            int userNumber = userNumberMap.getOrDefault(userId, 0);
                            sendText(chatId, "✅ #" + userNumber + " foydalanuvchining ballari 0 ga tushirildi!");

                            sendText(userId, "⚠️ Admin tomonidan barcha ballaringiz 0 ga tushirildi. " +
                                    "Yangi ballar to'plash uchun takliflaringizni davom ettiring!");

                            sendKonkursTakliflar(chatId);
                        }
                        return;
                    }
                    break;
            }
        }

        // ==================== YANGI FUNKSIYALAR ====================

        // 1. Teknik admin uchun foydalanuvchi boshqarish
        private void sendTechAdminUserManagement(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👤 *Foydalanuvchini boshqarish*\n\n" +
                    "Bloklangan foydalanuvchilar: " + bannedUsers.size() + " ta\n\n" +
                    "Quyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // 1. Foydalanuvchini bloklash
            InlineKeyboardButton blockBtn = new InlineKeyboardButton();
            blockBtn.setText("⛔ Foydalanuvchini bloklash");
            blockBtn.setCallbackData("tech_block_user");
            rows.add(Collections.singletonList(blockBtn));

            // 2. Blokdan chiqarish
            InlineKeyboardButton unblockBtn = new InlineKeyboardButton();
            unblockBtn.setText("✅ Blokdan chiqarish");
            unblockBtn.setCallbackData("tech_unblock_user");
            rows.add(Collections.singletonList(unblockBtn));

            // 3. Foydalanuvchini izlash
            InlineKeyboardButton searchBtn = new InlineKeyboardButton();
            searchBtn.setText("🔍 Foydalanuvchini izlash");
            searchBtn.setCallbackData("tech_search_user");
            rows.add(Collections.singletonList(searchBtn));

            // 4. Taklif qilganlarni izlash
            InlineKeyboardButton searchRefBtn = new InlineKeyboardButton();
            searchRefBtn.setText("🔗 Taklif qilganlarni izlash");
            searchRefBtn.setCallbackData("tech_search_referral");
            rows.add(Collections.singletonList(searchRefBtn));

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_admin_panel");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // 2. Bloklangan foydalanuvchilarni ko'rsatish
        private void showBlockedUsers(long adminId) throws TelegramApiException {
            if (bannedUsers.isEmpty()) {
                sendText(adminId, "📭 Bloklangan foydalanuvchilar yo'q.");
                return;
            }

            StringBuilder blockedList = new StringBuilder();
            blockedList.append("⛔ *Bloklangan foydalanuvchilar:*\n\n");

            int index = 1;
            for (Long userId : bannedUsers) {
                int userNumber = userNumberMap.getOrDefault(userId, 0);
                String username = userUsernameMap.getOrDefault(userId, "Noma'lum");
                blockedList.append(index).append(". #").append(userNumber)
                        .append(" - ").append(username).append("\n");
                index++;
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText(blockedList.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Har bir bloklangan foydalanuvchi uchun tugma
            index = 1;
            for (Long userId : bannedUsers) {
                InlineKeyboardButton unblockBtn = new InlineKeyboardButton();
                int userNumber = userNumberMap.getOrDefault(userId, 0);
                unblockBtn.setText("✅ #" + userNumber + " ni blokdan chiqarish");
                unblockBtn.setCallbackData("unblock_user_" + index);
                rows.add(Collections.singletonList(unblockBtn));
                index++;
            }

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_user_management");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // 3. Foydalanuvchini blokdan chiqarish
        private void unblockUserByIndex(long adminId, int index) throws TelegramApiException {
            int currentIndex = 1;
            Long userIdToUnblock = null;

            for (Long userId : bannedUsers) {
                if (currentIndex == index) {
                    userIdToUnblock = userId;
                    break;
                }
                currentIndex++;
            }

            if (userIdToUnblock == null) {
                sendText(adminId, "❌ Foydalanuvchi topilmadi!");
                return;
            }

            bannedUsers.remove(userIdToUnblock);
            int userNumber = userNumberMap.getOrDefault(userIdToUnblock, 0);

            sendText(userIdToUnblock, "✅ Sizning blokingiz olindi! Endi botdan foydalanishingiz mumkin.");
            sendText(adminId, "✅ #" + userNumber + " foydalanuvchi blokdan chiqarildi.");

            showBlockedUsers(adminId);
        }

        // 4. Admin bilan bog'lanish menyusi
        private void sendAdminContactMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👤 *Admin bilan bog'lanish*\n\n" +
                    "Qaysi admin bilan bog'lanmoqchisiz?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // 1. Kanal egasi
            InlineKeyboardButton channelAdminBtn = new InlineKeyboardButton();
            channelAdminBtn.setText("📢 Kanal egasi & Bot admini");
            channelAdminBtn.setCallbackData("contact_channel_admin");
            rows.add(Collections.singletonList(channelAdminBtn));

            // 2. Teknik admin
            InlineKeyboardButton techAdminBtn = new InlineKeyboardButton();
            techAdminBtn.setText("👨‍💻 Teknik admin (taklif/shikoyat)");
            techAdminBtn.setCallbackData("contact_tech_admin");
            rows.add(Collections.singletonList(techAdminBtn));

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // 5. Ro'yxatdan o'tishni yakunlash (tugmani o'chirish)
        private void completeRegistration(long chatId) throws TelegramApiException {
            userRegisteredMap.put(chatId, true);
            int userNumber = userCounter.getAndIncrement();
            userNumberMap.put(chatId, userNumber);
            userAdCountMap.put(chatId, 0);
            addScoreToUser(chatId, 10); // Boshlang'ich ball

            // REFERRAL BALL BERISH (agar referral orqali kirgan bo'lsa)
            if (referralMap.containsKey(chatId)) {
                Long referrerId = referralMap.get(chatId);
                if (referrerId != null) {
                    addScoreToUser(referrerId, 1);

                    int referrerNumber = userNumberMap.getOrDefault(referrerId, 0);
                    String referrerUsername = userUsernameMap.getOrDefault(referrerId, "");

                    String newUserInfo = "👤 @" + (userUsernameMap.getOrDefault(chatId, "Anonim").replace("@", ""))
                            + " (#" + userNumber + ")";

                    sendText(referrerId, "🎉 Tabriklaymiz! Sizning referral linkingiz orqali yangi foydalanuvchi qo'shildi:\n" +
                            newUserInfo + "\n\n" +
                            "✅ Sizga +1 ball qo'shildi!\n" +
                            "🏆 Jami ball: " + getUserScore(referrerId) + "\n" +
                            "👥 Jami takliflar: " + getReferralCount(referrerId) + " kishi");

                    // Texnik adminga ham xabar yuborish
                    sendText(TECHNICAL_ADMIN_ID, "🔗 Yangi referral!\n" +
                            "👤 Referrer: #" + referrerNumber + " @" + referrerUsername.replace("@", "") + "\n" +
                            "👥 Yangi: #" + userNumber + " @" + userUsernameMap.getOrDefault(chatId, "Anonim").replace("@", "") + "\n" +
                            "⏰ Vaqt: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
                }
            }

            // Tugmani o'chirish
            try {
                ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                keyboardRemove.setRemoveKeyboard(true);

                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(chatId));
                msg.setText("✅ Ro'yxatdan muvaffaqiyatli o'tdingiz!\n" +
                        "🔢 Sizning raqamingiz: #" + userNumber + "\n" +
                        "🏆 Ballaringiz: " + getUserScore(chatId) + "\n" +
                        "👥 Takliflaringiz: " + getReferralCount(chatId) + " kishi");
                msg.setReplyMarkup(keyboardRemove);
                execute(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Takliflar sonini hisoblash
        private int getReferralCount(long userId) {
            return (int) referralMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(userId))
                    .count();
        }

        // 6. Konkurs reytingi
        private void sendKonkursRating(long chatId) throws TelegramApiException {
            List<Map.Entry<Long, Integer>> sortedScores = userScores.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (sortedScores.isEmpty()) {
                sendText(chatId, "📊 Hozircha hech kim ball to'plamagan.");
                return;
            }

            StringBuilder ratingText = new StringBuilder();
            ratingText.append("🏆 *TOP 10 ISHTIROKCHILAR:*\n\n");

            for (int i = 0; i < sortedScores.size(); i++) {
                Map.Entry<Long, Integer> entry = sortedScores.get(i);
                Long userId = entry.getKey();
                Integer score = entry.getValue();

                String phone = userPhones.getOrDefault(userId, "Telefon yo'q");
                String username = userUsernameMap.getOrDefault(userId, "Anonim");
                int userNumber = userNumberMap.getOrDefault(userId, 0);

                if (username.startsWith("@")) {
                    username = username.substring(1);
                }

                // Taklif qilgan odamlar sonini hisoblash
                int referralsCount = getReferralCount(userId);

                String medal = "";
                if (i == 0) medal = "🥇";
                else if (i == 1) medal = "🥈";
                else if (i == 2) medal = "🥉";
                else medal = "👤";

                ratingText.append(medal).append(" ").append(i + 1).append(". #").append(userNumber)
                        .append("\n👤 Username: @").append(username)
                        .append("\n📱 Telefon: ").append(phone)
                        .append("\n🏆 Ball: ").append(score).append(" ball")
                        .append("\n👥 Takliflar: ").append(referralsCount).append(" kishi")
                        .append("\n──────────────────\n");
            }

            int userScore = getUserScore(chatId);
            int userRank = getUserRank(chatId);
            int userReferrals = getReferralCount(chatId);

            ratingText.append("\n📊 *Sizning statistikangiz:*\n");
            ratingText.append("🏆 Ball: ").append(userScore).append(" ball 🎯\n");
            ratingText.append("📈 O'rin: ").append(userRank).append("-o'rin\n");
            ratingText.append("👥 Taklif qilganlar: ").append(userReferrals).append(" kishi");

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(ratingText.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Taklif qilgan odamlarni ko'rish tugmasi
            InlineKeyboardButton referralsBtn = new InlineKeyboardButton();
            referralsBtn.setText("👥 Taklif qilganlarimni ko'rish");
            referralsBtn.setCallbackData("view_my_referrals");
            rows.add(Collections.singletonList(referralsBtn));

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Foydalanuvchi uchun taklif qilganlarini ko'rish funksiyasi
        private void showMyReferrals(long chatId) throws TelegramApiException {
            List<Long> myReferrals = referralMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(chatId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (myReferrals.isEmpty()) {
                sendText(chatId, "📭 Siz hali hech kimni taklif qilmagansiz.\n\n" +
                        "Taklif qilish uchun konkurs shartlariga o'ting va referal linkingizni do'stlaringizga ulashing.");
                return;
            }

            StringBuilder referralsText = new StringBuilder();
            referralsText.append("👥 *Sizning taklif qilgan odamlaringiz:*\n");
            referralsText.append("📊 Jami: ").append(myReferrals.size()).append(" kishi\n\n");

            for (int i = 0; i < myReferrals.size(); i++) {
                Long refId = myReferrals.get(i);
                String username = userUsernameMap.getOrDefault(refId, "Noma'lum");
                int userNumber = userNumberMap.getOrDefault(refId, 0);
                String phone = userPhones.getOrDefault(refId, "Telefon yo'q");

                referralsText.append(i + 1).append(". #").append(userNumber)
                        .append(" - @").append(username.startsWith("@") ? username.substring(1) : username)
                        .append("\n   📱 ").append(phone)
                        .append("\n   🆔 ").append(refId)
                        .append("\n──────────────────\n");
            }

            referralsText.append("\n🎉 Tabriklaymiz! Siz ").append(myReferrals.size())
                    .append(" kishini taklif qilib, ").append(myReferrals.size())
                    .append(" ball to'pladingiz!");

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(referralsText.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("konkurs_reting");

            markup.setKeyboard(Collections.singletonList(Collections.singletonList(backBtn)));
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Reytingni hisoblash
        private int getUserRank(long userId) {
            int userScore = getUserScore(userId);
            int rank = 1;

            for (Integer score : userScores.values()) {
                if (score > userScore) {
                    rank++;
                }
            }

            return rank;
        }

        // 7. Konkurs takliflar (teknik admin uchun)
        private void sendKonkursTakliflar(long chatId) throws TelegramApiException {
            // Top 10 ni olish
            List<Map.Entry<Long, Integer>> topUsers = userScores.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (topUsers.isEmpty()) {
                sendText(chatId, "📭 Hozircha konkurs ishtirokchilari yo'q.");
                return;
            }

            StringBuilder ratingText = new StringBuilder();
            ratingText.append("🏆 *Konkurs Top 10 - Admin panel:*\n\n");

            for (int i = 0; i < topUsers.size(); i++) {
                Map.Entry<Long, Integer> entry = topUsers.get(i);
                Long userId = entry.getKey();
                Integer score = entry.getValue();

                int userNumber = userNumberMap.getOrDefault(userId, 0);
                String username = userUsernameMap.getOrDefault(userId, "Noma'lum");
                String phone = userPhones.getOrDefault(userId, "Telefon yo'q");

                // Taklif qilgan odamlar sonini hisoblash
                int referralsCount = getReferralCount(userId);

                ratingText.append(i + 1).append(". #").append(userNumber)
                        .append("\n👤 @").append(username.startsWith("@") ? username.substring(1) : username)
                        .append("\n📱 ").append(phone)
                        .append("\n🏆 Ball: ").append(score)
                        .append("\n👥 Takliflar: ").append(referralsCount).append(" kishi")
                        .append("\n\n");
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(ratingText.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Har bir foydalanuvchi uchun tugma (1 dan 10 gacha)
            for (int i = 1; i <= 10; i++) {
                if (i <= topUsers.size()) {
                    InlineKeyboardButton userBtn = new InlineKeyboardButton();
                    userBtn.setText("👤 #" + userNumberMap.getOrDefault(topUsers.get(i-1).getKey(), i));
                    userBtn.setCallbackData("tech_ref_details_" + i);
                    rows.add(Collections.singletonList(userBtn));
                }
            }

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_admin_panel");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Detal ma'lumotlarni ko'rsatish (4-chi odamni tanlasa)
        private void showReferralDetailsForAdmin(long adminId, int rank) throws TelegramApiException {
            // Top 10 ni olish
            List<Map.Entry<Long, Integer>> topUsers = userScores.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (rank > topUsers.size() || rank < 1) {
                sendText(adminId, "❌ " + rank + "-o'rin mavjud emas!");
                return;
            }

            Map.Entry<Long, Integer> userEntry = topUsers.get(rank - 1);
            Long userId = userEntry.getKey();

            // Ushbu foydalanuvchi qo'shgan odamlarni topish
            List<Long> referrals = referralMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(userId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            StringBuilder result = new StringBuilder();
            result.append("📊 *DETAL STATISTIKA*\n\n");
            result.append("👤 Foydalanuvchi: #").append(userNumberMap.getOrDefault(userId, 0))
                    .append(" (").append(rank).append("-o'rin)\n");
            result.append("📱 Username: @").append(userUsernameMap.getOrDefault(userId, "Noma'lum")
                            .replace("@", ""))
                    .append("\n");
            result.append("📞 Telefon: ").append(userPhones.getOrDefault(userId, "Noma'lum"))
                    .append("\n");
            result.append("🆔 Telegram ID: ").append(userId)
                    .append("\n");
            result.append("🏆 Ball: ").append(userEntry.getValue())
                    .append("\n");
            result.append("👥 Takliflar soni: ").append(referrals.size()).append(" kishi\n\n");

            if (!referrals.isEmpty()) {
                result.append("🔢 *Taklif qilgan odamlar ro'yxati:*\n\n");

                for (int i = 0; i < referrals.size(); i++) {
                    Long refId = referrals.get(i);
                    String username = userUsernameMap.getOrDefault(refId, "Noma'lum");
                    int userNumber = userNumberMap.getOrDefault(refId, 0);
                    String phone = userPhones.getOrDefault(refId, "Telefon yo'q");

                    result.append(i + 1).append(". #").append(userNumber)
                            .append(" - @").append(username.startsWith("@") ? username.substring(1) : username)
                            .append("\n   📱 ").append(phone)
                            .append("\n   🆔 ").append(refId)
                            .append("\n   ──────────────────\n");
                }
            } else {
                result.append("⚠️ *Hali hech kim taklif qilmagan*");
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText(result.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Ballarni 0 qilish tugmasi
            InlineKeyboardButton resetBtn = new InlineKeyboardButton();
            resetBtn.setText("🔄 Ballarni 0 qilish");
            resetBtn.setCallbackData("tech_reset_scores_" + userId);
            rows.add(Collections.singletonList(resetBtn));

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_konkurs_takliflar");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // 8. Texnik admin uchun detallı statistika
        private void sendDetailedStats(long adminId) throws TelegramApiException {
            int totalUsers = userNumberMap.size();
            int totalActiveUsers = userScores.size();
            int totalReferrals = referralMap.size();

            // Top referrer ni topish
            Map<Long, Long> referralCounts = new HashMap<>();
            for (Long referee : referralMap.values()) {
                referralCounts.put(referee, referralCounts.getOrDefault(referee, 0L) + 1);
            }

            Map.Entry<Long, Long> topReferrer = referralCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            StringBuilder stats = new StringBuilder();
            stats.append("📊 *DETALLI STATISTIKA*\n\n");
            stats.append("👥 *Foydalanuvchilar:*\n");
            stats.append("• Jami ro'yxatdan o'tganlar: ").append(totalUsers).append(" ta\n");
            stats.append("• Aktiv (ball to'plaganlar): ").append(totalActiveUsers).append(" ta\n");
            stats.append("• Umumiy takliflar: ").append(totalReferrals).append(" ta\n\n");

            stats.append("🏆 *Konkurs statistikasi:*\n");
            stats.append("• O'rtacha ball: ").append(calculateAverageScore()).append("\n");
            stats.append("• Eng yuqori ball: ").append(getMaxScore()).append("\n\n");

            if (topReferrer != null) {
                String topUsername = userUsernameMap.getOrDefault(topReferrer.getKey(), "Noma'lum");
                stats.append("👑 *Top referrer:*\n");
                stats.append("• @").append(topUsername.replace("@", ""))
                        .append(" (#").append(userNumberMap.getOrDefault(topReferrer.getKey(), 0)).append(")\n");
                stats.append("• Takliflar: ").append(topReferrer.getValue()).append(" kishi\n");
            }

            stats.append("\n📅 *Oxirgi yangilanish:* ").append(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText(stats.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_admin_panel");

            markup.setKeyboard(Collections.singletonList(Collections.singletonList(backBtn)));
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Yordamchi metodlar
        private double calculateAverageScore() {
            if (userScores.isEmpty()) return 0;
            double sum = userScores.values().stream().mapToInt(Integer::intValue).sum();
            return Math.round((sum / userScores.size()) * 10.0) / 10.0;
        }

        private int getMaxScore() {
            return userScores.values().stream().max(Integer::compareTo).orElse(0);
        }

        // Taklif qilgan odamlarni ID bo'yicha qidirish
        private void searchReferralsById(long adminId, long searchId) throws TelegramApiException {
            // ID bo'yicha referral qilgan odamni topish
            Long referrerId = referralMap.get(searchId);

            if (referrerId == null) {
                sendText(adminId, "❌ #" + userNumberMap.getOrDefault(searchId, 0) +
                        " IDli foydalanuvchi hech kim tomonidan taklif qilinmagan.");
                return;
            }

            String referrerUsername = userUsernameMap.getOrDefault(referrerId, "Noma'lum");
            int referrerNumber = userNumberMap.getOrDefault(referrerId, 0);
            int referrerScore = userScores.getOrDefault(referrerId, 0);

            StringBuilder result = new StringBuilder();
            result.append("🔍 *REFERRAL MA'LUMOTLARI*\n\n");
            result.append("👤 *Taklif qilgan shaxs:*\n");
            result.append("• #").append(referrerNumber).append(" @").append(referrerUsername.replace("@", ""))
                    .append("\n• 🆔 ID: ").append(referrerId)
                    .append("\n• 🏆 Ball: ").append(referrerScore)
                    .append("\n• 📞 Telefon: ").append(userPhones.getOrDefault(referrerId, "Noma'lum"))
                    .append("\n\n");

            // Taklif qilgan barcha odamlarni ko'rsatish
            List<Long> referrals = referralMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(referrerId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!referrals.isEmpty()) {
                result.append("👥 *Taklif qilgan odamlar:*\n\n");
                for (Long refId : referrals) {
                    String username = userUsernameMap.getOrDefault(refId, "Noma'lum");
                    result.append("• #").append(userNumberMap.getOrDefault(refId, 0))
                            .append(" @").append(username.replace("@", ""))
                            .append(" 🆔 ").append(refId).append("\n");
                }
            }

            sendText(adminId, result.toString());
        }

        // Texnik admin panelini yangilaymiz
        private void sendTechnicalAdminMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👨‍💻 *Teknik Admin Paneli*\n\n" +
                    "Jami ro'yxatdan o'tganlar: " + userNumberMap.size() + " ta\n" +
                    "Jami reklamalar: " + userAdsMap.values().stream().mapToInt(List::size).sum() + " ta\n" +
                    "Bloklanganlar: " + bannedUsers.size() + " ta\n" +
                    "Konkurs ishtirokchilari: " + userScores.size() + " ta\n\n" +
                    "Quyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton userManagementBtn = new InlineKeyboardButton();
            userManagementBtn.setText("👤 Foydalanuvchi boshqarish");
            userManagementBtn.setCallbackData("tech_user_management");
            rows.add(Collections.singletonList(userManagementBtn));

            InlineKeyboardButton konkursBtn = new InlineKeyboardButton();
            konkursBtn.setText("🏆 Konkurs takliflar");
            konkursBtn.setCallbackData("tech_konkurs_takliflar");
            rows.add(Collections.singletonList(konkursBtn));

            InlineKeyboardButton adBtn = new InlineKeyboardButton();
            adBtn.setText("📢 Foydalanuvchilarga Reklama");
            adBtn.setCallbackData("ad_panel");
            rows.add(Collections.singletonList(adBtn));

            InlineKeyboardButton referralsBtn = new InlineKeyboardButton();
            referralsBtn.setText("📊 Detal statistika");
            referralsBtn.setCallbackData("tech_detailed_stats");
            rows.add(Collections.singletonList(referralsBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Asosiy menyu");
            backBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // ESKI METODLAR (YANGILANGAN) ====================

        // Foydalanuvchi reklamalarini sahifalangan holda ko'rsatish
        private void showUserAdsInPages(long chatId) throws TelegramApiException {
            cleanupExpiredAds();

            List<UserAd> userAds = userAdsMap.getOrDefault(chatId, new ArrayList<>());
            List<UserAd> activeAds = userAds.stream()
                    .filter(ad -> !ad.isExpired())
                    .collect(Collectors.toList());

            if (activeAds.isEmpty()) {
                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(chatId));
                msg.setText("📭 Sizda hali e'lonlar mavjud emas yoki barcha e'lonlar muddati tugagan.\n\n" +
                        "Birinchi e'loningizni joylash uchun \"📢 Reklama joylash\" tugmasini bosing.");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton backBtn = new InlineKeyboardButton();
                backBtn.setText("🏠 Asosiy menyu");
                backBtn.setCallbackData("menu_main");

                markup.setKeyboard(Collections.singletonList(Collections.singletonList(backBtn)));
                msg.setReplyMarkup(markup);
                execute(msg);
                return;
            }

            showUserAdsInPagesWithPage(chatId, 0);
        }

        // Sahifalangan reklamalarni ko'rsatish
        private void showUserAdsInPagesWithPage(long chatId, int pageNumber) throws TelegramApiException {
            List<UserAd> userAds = userAdsMap.getOrDefault(chatId, new ArrayList<>());
            List<UserAd> activeAds = userAds.stream()
                    .filter(ad -> !ad.isExpired())
                    .collect(Collectors.toList());

            if (activeAds.isEmpty()) {
                sendText(chatId, "📭 Sizda hali e'lonlar mavjud emas.");
                return;
            }

            int itemsPerPage = 5;
            int startIndex = pageNumber * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, activeAds.size());
            int totalPages = (int) Math.ceil((double) activeAds.size() / itemsPerPage);

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📋 Mening e'lonlarim (" + activeAds.size() + " ta)\n" +
                    "Sahifa: " + (pageNumber + 1) + "/" + totalPages);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = startIndex; i < endIndex; i++) {
                UserAd ad = activeAds.get(i);
                String adTypeEmoji = getAdTypeEmoji(ad.adType);
                String adStatus = ad.isActive ? "✅ Faol" : "❌ Nofaol";

                String buttonText = adTypeEmoji + " Reklama #" + ad.adNumber;
                if (!ad.isActive) {
                    buttonText += " (" + (ad.adType.equals("hadiya") ? "Berib bo'lindik" : "Sotildi") + ")";
                }

                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(buttonText);
                btn.setCallbackData("view_ad_" + ad.adId);
                rows.add(Collections.singletonList(btn));
            }

            if (totalPages > 1) {
                List<InlineKeyboardButton> navRow = new ArrayList<>();

                if (pageNumber > 0) {
                    InlineKeyboardButton prevBtn = new InlineKeyboardButton();
                    prevBtn.setText("⬅️ Oldingi");
                    prevBtn.setCallbackData("ads_page_" + (pageNumber - 1));
                    navRow.add(prevBtn);
                }

                InlineKeyboardButton pageBtn = new InlineKeyboardButton();
                pageBtn.setText("📄 " + (pageNumber + 1) + "/" + totalPages);
                pageBtn.setCallbackData("current_page");
                navRow.add(pageBtn);

                if (pageNumber < totalPages - 1) {
                    InlineKeyboardButton nextBtn = new InlineKeyboardButton();
                    nextBtn.setText("Keyingi ➡️");
                    nextBtn.setCallbackData("ads_page_" + (pageNumber + 1));
                    navRow.add(nextBtn);
                }

                rows.add(navRow);
            }

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("🏠 Asosiy menyu");
            backBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Har bir reklamani alohida ko'rsatish
        private void showIndividualAd(long chatId, long adId) throws TelegramApiException {
            List<UserAd> userAds = userAdsMap.get(chatId);
            UserAd targetAd = null;

            if (userAds != null) {
                for (UserAd ad : userAds) {
                    if (ad.adId == adId) {
                        targetAd = ad;
                        break;
                    }
                }
            }

            if (targetAd == null) {
                sendText(chatId, "❌ E'lon topilmadi!");
                showUserAdsInPages(chatId);
                return;
            }

            String message = buildAdDetailsMessage(targetAd);

            if (targetAd.photos != null && !targetAd.photos.isEmpty() && !targetAd.photos.get(0).startsWith("video:")) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(chatId));
                photo.setPhoto(new InputFile(targetAd.photos.get(0)));
                photo.setCaption(message);

                InlineKeyboardMarkup markup = createAdButtons(targetAd, chatId);
                photo.setReplyMarkup(markup);
                execute(photo);
            } else {
                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(chatId));
                msg.setText(message);

                InlineKeyboardMarkup markup = createAdButtons(targetAd, chatId);
                msg.setReplyMarkup(markup);
                execute(msg);
            }
        }

        // Reklama tugmalarini yaratish
        private InlineKeyboardMarkup createAdButtons(UserAd ad, long chatId) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            if (ad.isActive) {
                if ("hadiya".equals(ad.adType)) {
                    InlineKeyboardButton completedBtn = new InlineKeyboardButton();
                    completedBtn.setText("✅ Berib bo'ldim");
                    completedBtn.setCallbackData("mark_completed_" + ad.adId);
                    rows.add(Collections.singletonList(completedBtn));
                } else if ("sotish".equals(ad.adType)) {
                    InlineKeyboardButton soldBtn = new InlineKeyboardButton();
                    soldBtn.setText("💰 Sotildi");
                    soldBtn.setCallbackData("mark_sold_" + ad.adId);
                    rows.add(Collections.singletonList(soldBtn));
                }
            }

            if (ad.isActive && ad.channelMessageId != null) {
                InlineKeyboardButton viewInChannelBtn = new InlineKeyboardButton();
                viewInChannelBtn.setText("👁️ Reklamani ko'rish");
                viewInChannelBtn.setUrl(ad.getChannelLink());
                rows.add(Collections.singletonList(viewInChannelBtn));
            }

            List<InlineKeyboardButton> bottomRow = new ArrayList<>();

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("back_to_ads_list");
            bottomRow.add(backBtn);

            InlineKeyboardButton mainBtn = new InlineKeyboardButton();
            mainBtn.setText("🏠 Asosiy menyu");
            mainBtn.setCallbackData("menu_main");
            bottomRow.add(mainBtn);

            rows.add(bottomRow);

            markup.setKeyboard(rows);
            return markup;
        }

        // Reklama ma'lumotlarini qurish
        private String buildAdDetailsMessage(UserAd ad) {
            StringBuilder sb = new StringBuilder();

            String adTypeText = "";
            String adTypeEmoji = "";
            switch (ad.adType) {
                case "sotish":
                    adTypeText = "SOTILADI";
                    adTypeEmoji = "💰";
                    break;
                case "hadiya":
                    adTypeText = "HADIYAGA";
                    adTypeEmoji = "🎁";
                    break;
                case "vyazka":
                    adTypeText = "VYAZKAGA";
                    adTypeEmoji = "💝";
                    break;
            }

            sb.append(adTypeEmoji).append(" *").append(adTypeText).append("* (Reklama #").append(ad.adNumber).append(")\n\n");

            if (!ad.breed.isEmpty()) sb.append("🐱 *Zot:* ").append(ad.breed).append("\n");
            if (!ad.age.isEmpty()) sb.append("🎂 *Yosh:* ").append(ad.age).append("\n");
            if (!ad.gender.isEmpty()) sb.append("👤 *Jins:* ").append(ad.gender).append("\n");
            if (!ad.health.isEmpty()) sb.append("❤️ *Sog'lig'i:* ").append(ad.health).append("\n");
            if (!ad.sterilization.isEmpty()) sb.append("🧬 *Nasl olish:* ").append(ad.sterilization).append("\n");

            sb.append("📍 *Manzil:* ").append(ad.manzil).append("\n");

            if (!ad.price.isEmpty()) {
                String valyutaSign = "so'm".equals(ad.valyuta) ? " so'm" : " $";
                sb.append("💰 *Narx:* ").append(ad.price).append(valyutaSign).append("\n");
            }

            if (ad.isActive) {
                sb.append("📞 *Telefon:* ").append(ad.phone).append("\n\n");
            } else {
                String statusText = "hadiya".equals(ad.adType) ? "✅ Berib bo'lindik" : "💰 Sotildi";
                sb.append("📞 *Holat:* ").append(statusText).append("\n\n");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            sb.append("⏰ *Joylangan:* ").append(sdf.format(ad.createdDate)).append("\n");

            long daysLeft = 10 - ((new Date().getTime() - ad.createdDate.getTime()) / (24 * 60 * 60 * 1000));
            if (daysLeft > 0) {
                sb.append("📅 *Muddati:* ").append(daysLeft).append(" kun qoldi");
            } else {
                sb.append("⚠️ *Muddati tugagan!*");
            }

            return sb.toString();
        }

        // Reklamani "Berib bo'lindi" deb belgilash
        private void markAdAsCompleted(long chatId, long adId) throws TelegramApiException {
            List<UserAd> userAds = userAdsMap.get(chatId);

            if (userAds != null) {
                for (UserAd ad : userAds) {
                    if (ad.adId == adId) {
                        ad.isActive = false;
                        ad.phone = "✅ Berib bo'lindik";

                        // Kanaldagi reklamani yangilash
                        updateChannelAd(chatId, adId, "berib_bo'lindik");

                        // Adminlarga xabar yuborish
                        String adminMessage = "📢 REKLAMA YAKUNLANDI\n\n" +
                                "Foydalanuvchi: #" + userNumberMap.getOrDefault(chatId, 0) + "\n" +
                                "Reklama #: " + ad.adNumber + "\n" +
                                "Status: ✅ Berib bo'lindik";

                        sendText(ADMIN_ID, adminMessage);
                        if (chatId != TECHNICAL_ADMIN_ID) {
                            sendText(TECHNICAL_ADMIN_ID, adminMessage);
                        }

                        sendText(chatId, "✅ Reklama \"Berib bo'lindi\" deb belgilandi!");

                        showIndividualAd(chatId, adId);
                        break;
                    }
                }
            }
        }

        // Reklamani "Sotildi" deb belgilash
        private void markAdAsSold(long chatId, long adId) throws TelegramApiException {
            List<UserAd> userAds = userAdsMap.get(chatId);

            if (userAds != null) {
                for (UserAd ad : userAds) {
                    if (ad.adId == adId) {
                        ad.isActive = false;
                        ad.phone = "💰 Sotildi";

                        // Kanaldagi reklamani yangilash
                        updateChannelAd(chatId, adId, "sotildi");

                        // Adminlarga xabar yuborish
                        String adminMessage = "📢 REKLAMA YAKUNLANDI\n\n" +
                                "Foydalanuvchi: #" + userNumberMap.getOrDefault(chatId, 0) + "\n" +
                                "Reklama #: " + ad.adNumber + "\n" +
                                "Status: 💰 Sotildi";

                        sendText(ADMIN_ID, adminMessage);
                        if (chatId != TECHNICAL_ADMIN_ID) {
                            sendText(TECHNICAL_ADMIN_ID, adminMessage);
                        }

                        sendText(chatId, "✅ Reklama \"Sotildi\" deb belgilandi!");

                        showIndividualAd(chatId, adId);
                        break;
                    }
                }
            }
        }

        // Kanal havolasini ko'rsatish
        private void showChannelAdLink(long chatId, long adId) {
            List<UserAd> userAds = userAdsMap.get(chatId);

            if (userAds != null) {
                for (UserAd ad : userAds) {
                    if (ad.adId == adId && ad.channelMessageId != null) {
                        String channelLink = ad.getChannelLink();
                        try {
                            sendText(chatId, "🔗 Reklamangiz kanalda:\n\n" + channelLink);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }

            try {
                sendText(chatId, "❌ Bu reklama kanalda topilmadi.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Kanaldagi reklamani yangilash
        private void updateChannelAd(long userId, long adId, String status) throws TelegramApiException {
            List<UserAd> userAds = userAdsMap.get(userId);
            if (userAds == null) return;

            UserAd targetAd = null;
            for (UserAd ad : userAds) {
                if (ad.adId == adId) {
                    targetAd = ad;
                    break;
                }
            }

            if (targetAd == null || targetAd.channelMessageId == null) return;

            try {
                EditMessageCaption editCaption = new EditMessageCaption();
                editCaption.setChatId(CHANNEL_USERNAME);
                editCaption.setMessageId(targetAd.channelMessageId);

                String newCaption = buildUpdatedChannelCaption(targetAd, status);
                editCaption.setCaption(newCaption);
                editCaption.setParseMode("Markdown");

                execute(editCaption);

            } catch (Exception e) {
                System.out.println("Kanaldagi reklamani yangilashda xatolik: " + e.getMessage());
            }
        }

        // Yangilangan kanal caption
        private String buildUpdatedChannelCaption(UserAd ad, String status) {
            StringBuilder caption = new StringBuilder();

            if ("hadiya".equals(ad.adType)) {
                caption.append("#HADIYA 🎁\n\n");
                caption.append("📝 Mushukcha yaxshi insonlarga tekinga sovg'a qilinadi.\n\n");
                caption.append("📍 Manzil: ").append(ad.manzil).append("\n");
                if ("berib_bo'lindi".equals(status)) {
                    caption.append("✅ BERIB BO'LDI\n\n");
                } else {
                    caption.append("📞 Nomer: ").append(ad.phone).append("\n\n");
                }
            } else if ("vyazka".equals(ad.adType)) {
                caption.append("#VYAZKA 💝\n\n");
                caption.append("📝").append(ad.breed).append(" ").append(ad.age).append(" ").append(ad.gender.toLowerCase())
                        .append(" ").append(ad.sterilization).append("\n\n");
                caption.append("📍 Manzil: ").append(ad.manzil).append("\n");
                caption.append("📞 Nomer: ").append(ad.phone).append("\n\n");
            } else {
                caption.append("#SOTILADI 💰\n\n");
                caption.append("📝").append(ad.breed).append(" ").append(ad.age).append(" ").append(ad.gender.toLowerCase())
                        .append(" ").append(ad.sterilization).append("\n\n");
                caption.append("📍 Manzil: ").append(ad.manzil).append("\n");
                if ("sotildi".equals(status)) {
                    caption.append("✅ SOTILDI\n");
                } else {
                    caption.append("💵 Narxi: ").append(ad.price).append(" so'm").append("\n");
                    caption.append("📞 Tel: ").append(ad.phone).append("\n");
                }
            }

            caption.append("\n👤 [Admin](https://t.me/zayd_catlover)\n");
            caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot?start=reklama)\n\n");
            caption.append("[YouTube](https://youtu.be/vdwgSB7_amw)");
            caption.append(" 🌐[Instagram](https://www.instagram.com/p/C-cZkgstVGK/)");
            caption.append(" ✉️[Telegram](https://t.me/uzbek_cats)");

            return caption.toString();
        }

        // Reklama turi bo'yicha emoji olish
        private String getAdTypeEmoji(String adType) {
            switch (adType) {
                case "sotish": return "💰";
                case "hadiya": return "🎁";
                case "vyazka": return "💝";
                default: return "📢";
            }
        }

        // 3 ta tugmalik menyu
        private void sendThreeButtonMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🎉 *Muvaffaqiyatli ro'yxatdan o'tdingiz!*\n\n" +
                    "🔢 Sizning raqamingiz: #" + userNumberMap.getOrDefault(chatId, 0) + "\n" +
                    "🏆 Ballaringiz: " + getUserScore(chatId) + "\n" +
                    "👥 Takliflaringiz: " + getReferralCount(chatId) + " kishi\n\n" +
                    "Quyidagi imkoniyatlardan foydalaning:");
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton adBtn = new InlineKeyboardButton();
            adBtn.setText("📢 Kanalga reklama bermoqchiman");
            adBtn.setCallbackData("menu_reklama");
            rows.add(Collections.singletonList(adBtn));

            InlineKeyboardButton konkursBtn = new InlineKeyboardButton();
            konkursBtn.setText("🏆 Konkursda ishtirok etmoqchiman");
            konkursBtn.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(konkursBtn));

            InlineKeyboardButton mainBtn = new InlineKeyboardButton();
            mainBtn.setText("🏠 Asosiy menyu");
            mainBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(mainBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Asosiy menyu
        private void sendMainMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🏠 *Asosiy menyu*\n\n" +
                    "Quyidagi imkoniyatlardan foydalaning:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton adBtn = new InlineKeyboardButton();
            adBtn.setText("📢 Kanalga reklama bermoqchiman");
            adBtn.setCallbackData("menu_reklama");
            rows.add(Collections.singletonList(adBtn));

            InlineKeyboardButton konkursBtn = new InlineKeyboardButton();
            konkursBtn.setText("🏆 Konkursda ishtirok etmoqchiman");
            konkursBtn.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(konkursBtn));

            InlineKeyboardButton aboutBtn = new InlineKeyboardButton();
            aboutBtn.setText("ℹ️ Bot haqida");
            aboutBtn.setCallbackData("menu_about");
            rows.add(Collections.singletonList(aboutBtn));

            InlineKeyboardButton adminBtn = new InlineKeyboardButton();
            adminBtn.setText("👤 Admin bilan bog'lanish");
            adminBtn.setCallbackData("menu_admin");
            rows.add(Collections.singletonList(adminBtn));

            InlineKeyboardButton ordersBtn = new InlineKeyboardButton();
            ordersBtn.setText("📋 Mening e'lonlarim");
            ordersBtn.setCallbackData("menu_my_orders");
            rows.add(Collections.singletonList(ordersBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_back_to_three");
            rows.add(Collections.singletonList(backBtn));

            if (ADMIN_IDS.contains(chatId)) {
                InlineKeyboardButton adminPanelBtn = new InlineKeyboardButton();
                adminPanelBtn.setText("👨‍💼 Admin paneli");
                adminPanelBtn.setCallbackData("admin_panel");
                rows.add(Collections.singletonList(adminPanelBtn));
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Telefon raqam so'rash
        private void sendPhoneRequest(long chatId) throws TelegramApiException {
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            keyboard.setOneTimeKeyboard(true);

            KeyboardRow row = new KeyboardRow();
            KeyboardButton phoneButton = new KeyboardButton("📞 Raqamni yuborish");
            phoneButton.setRequestContact(true);
            row.add(phoneButton);

            List<KeyboardRow> keyboardRows = new ArrayList<>();
            keyboardRows.add(row);
            keyboard.setKeyboard(keyboardRows);

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📱 *Assalomu alaykum! Uzbek Cats botiga xush kelibsiz!*\n\n" +
                    "Botdan foydalanish uchun telefon raqamingizni yuboring:\n\n" +
                    "Pastdagi \"📞 Raqamni yuborish\" tugmasini bosing yoki telefon raqamingizni kiriting.");
            msg.setReplyMarkup(keyboard);

            execute(msg);
        }

        // Konkurs shartlari
        private void sendKonkursShartlar(long chatId) throws TelegramApiException {
            String referralCode = generateReferralCode(chatId);
            String referralLink = "https://t.me/" + getBotUsername().replace("@", "") + "?start=" + referralCode;

            String shartlarText = "⬇️ *Qatnashish shartlari:*\n\n" +
                    "🔗 Bot sizga bergan referral linkni iloji boricha ko'proq do'stlaringizga ulashing.\n" +
                    "Sizni linkingizdan qo'shilgan har bir ishtirokchiga 1 ball beriladi.\n" +
                    "Sovg'alar eng ko'p ball to'plagan ishtirokchiga beriladi.\n\n" +
                    "🎁 *Mukofotlar:*\n" +
                    "🥇 Scottish fold black\n\n" +
                    "✅ *Qatnashish juda oson:*\n" +
                    "1. Botga /start bosing\n" +
                    "2. Kanallarga a'zo bo'ling\n" +
                    "3. Do'stlaringizni taklif qiling\n" +
                    "4. Eng ko'p ball to'plab, mukofotlarni qo'lga kiriting!\n\n" +
                    "🔗 *Sizning referral linkingiz:*\n" +
                    "`" + referralLink + "`\n\n" +
                    "📊 *Sizning ballingiz:* " + getUserScore(chatId) + " ball\n" +
                    "👥 *Sizning takliflaringiz:* " + getReferralCount(chatId) + " kishi";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(shartlarText);
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton shareBtn = new InlineKeyboardButton();
            shareBtn.setText("📤 Referral linkni ulashish");
            try {
                shareBtn.setUrl("https://t.me/share/url?url=" + URLEncoder.encode(referralLink, "UTF-8") +
                        "&text=" + URLEncoder.encode("Mushuklar konkursiga qo'shiling! Bu mening referral linkim:", "UTF-8"));
            } catch (Exception e) {
                shareBtn.setUrl("https://t.me/share/url?url=" + referralLink);
            }
            rows.add(Collections.singletonList(shareBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Referral code yaratish
        private String generateReferralCode(long userId) {
            String code = "REF" + userId + "_" + System.currentTimeMillis();
            referralCodes.put(userId, code);
            codeToUserMap.put(code, userId);
            return code;
        }

        // Ball qo'shish
        private void addScoreToUser(long userId, int score) {
            int currentScore = userScores.getOrDefault(userId, 0);
            userScores.put(userId, currentScore + score);
        }

        // Ballarni olish
        private int getUserScore(long userId) {
            return userScores.getOrDefault(userId, 0);
        }

        // Texnik adminga foydalanuvchi ma'lumotlarini yuborish
        private void sendUserInfoToTechnicalAdmin(long userId) throws TelegramApiException {
            String fullName = userFullNameMap.getOrDefault(userId, "Noma'lum");
            String phone = phoneMap.getOrDefault(userId, "Noma'lum");
            String username = userUsernameMap.getOrDefault(userId, "Noma'lum");
            int userNumber = userNumberMap.getOrDefault(userId, 0);

            String userInfo = "🆕 *#" + userNumber + " FOYDALANUVCHI RO'YXATDAN O'TTI*\n\n" +
                    "👤 *Ism-familiya:* " + fullName + "\n" +
                    "🔗 *Telegram:* " + username + "\n" +
                    "📞 *Telefon raqam:* " + phone + "\n" +
                    "🆔 *Telegram ID:* " + userId + "\n" +
                    "🏆 *Boshlang'ich ball:* 10\n" +
                    "⏰ *Ro'yxatdan o'tgan vaqt:* " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            sendText(TECHNICAL_ADMIN_ID, userInfo);
        }

        // Admin panel
        private void sendAdminPanel(long chatId) throws TelegramApiException {
            if (!ADMIN_IDS.contains(chatId)) {
                sendText(chatId, "❌ Siz admin emassiz!");
                return;
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👨‍💼 *Admin Paneliga xush kelibsiz!*\n\n" +
                    "Sizning ID: " + chatId + "\n" +
                    "Bot holati: " + (botEnabled ? "✅ Yoqilgan" : "❌ O'chirilgan") + "\n" +
                    "Bloklangan foydalanuvchilar: " + bannedUsers.size() + " ta\n" +
                    "Konkurs ishtirokchilari: " + userScores.size() + " ta\n\n" +
                    "Quyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton botControlBtn = new InlineKeyboardButton();
            botControlBtn.setText("⚙️ Bot ustroystiva");
            botControlBtn.setCallbackData("admin_bot_control");
            rows.add(Collections.singletonList(botControlBtn));

            InlineKeyboardButton banBtn = new InlineKeyboardButton();
            banBtn.setText("⛔ Foydalanuvchini bloklash");
            banBtn.setCallbackData("admin_ban_user");
            rows.add(Collections.singletonList(banBtn));

            InlineKeyboardButton statsBtn = new InlineKeyboardButton();
            statsBtn.setText("📊 Statistika");
            statsBtn.setCallbackData("admin_stats");
            rows.add(Collections.singletonList(statsBtn));

            InlineKeyboardButton konkursBtn = new InlineKeyboardButton();
            konkursBtn.setText("🏆 Konkursni o'zgartirish");
            konkursBtn.setCallbackData("admin_konkurs_change");
            rows.add(Collections.singletonList(konkursBtn));

            InlineKeyboardButton ratingBtn = new InlineKeyboardButton();
            ratingBtn.setText("🏆 Reyting boshqarish");
            ratingBtn.setCallbackData("admin_rating_manage");
            rows.add(Collections.singletonList(ratingBtn));

            if (chatId == TECHNICAL_ADMIN_ID) {
                InlineKeyboardButton techBtn = new InlineKeyboardButton();
                techBtn.setText("👨‍💻 Texnik admin");
                techBtn.setCallbackData("tech_admin_panel");
                rows.add(Collections.singletonList(techBtn));
            }

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Asosiy menyu");
            backBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Bot ustroystiva menyusi
        private void sendBotControlMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("⚙️ *Bot ustroystiva*\n\n" +
                    "Joriy holat: " + (botEnabled ? "✅ Yoqilgan" : "❌ O'chirilgan") + "\n" +
                    "Bloklangan foydalanuvchilar: " + bannedUsers.size() + " ta");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton toggleBtn = new InlineKeyboardButton();
            toggleBtn.setText(botEnabled ? "🔴 Botni o'chirish" : "🟢 Botni yoqish");
            toggleBtn.setCallbackData("admin_toggle_bot");
            rows.add(Collections.singletonList(toggleBtn));

            InlineKeyboardButton statsBtn = new InlineKeyboardButton();
            statsBtn.setText("📊 Bot statistikasi");
            statsBtn.setCallbackData("admin_bot_stats");
            rows.add(Collections.singletonList(statsBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("admin_panel");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Bot statistikasi
        private void sendBotStatistics(long adminId) throws TelegramApiException {
            int totalUsers = userNumberMap.size();
            int totalAds = userAdsMap.values().stream().mapToInt(List::size).sum();
            int hadiyaCount = statisticsMap.get("hadiya").size();
            int sotishCount = statisticsMap.get("sotish").size();
            int vyazkaCount = statisticsMap.get("vyazka").size();

            double hadiyaPercent = totalAds > 0 ? (hadiyaCount * 100.0 / totalAds) : 0;
            double sotishPercent = totalAds > 0 ? (sotishCount * 100.0 / totalAds) : 0;
            double vyazkaPercent = totalAds > 0 ? (vyazkaCount * 100.0 / totalAds) : 0;

            String hadiyaBar = createBarChart(hadiyaPercent, 20);
            String sotishBar = createBarChart(sotishPercent, 20);
            String vyazkaBar = createBarChart(vyazkaPercent, 20);

            String statsText = "📊 *BOT STATISTIKASI*\n\n" +
                    "👥 *Umumiy foydalanuvchilar:* " + totalUsers + " ta\n" +
                    "📢 *Umumiy reklamalar:* " + totalAds + " ta\n\n" +
                    "📈 *Reklama turlari bo'yicha:*\n\n" +
                    "🎁 *Hadiyaga berilgan:*\n" +
                    hadiyaBar + " " + String.format("%.1f", hadiyaPercent) + "% (" + hadiyaCount + " ta)\n\n" +
                    "💰 *Sotilgan:*\n" +
                    sotishBar + " " + String.format("%.1f", sotishPercent) + "% (" + sotishCount + " ta)\n\n" +
                    "💝 *Vyazkaga berilgan:*\n" +
                    vyazkaBar + " " + String.format("%.1f", vyazkaPercent) + "% (" + vyazkaCount + " ta)\n\n" +
                    "⏰ *Oxirgi yangilanish:* " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            sendText(adminId, statsText);
        }

        // Diagramma yaratish
        private String createBarChart(double percent, int length) {
            int filled = (int) Math.round(percent * length / 100.0);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (i < filled) {
                    bar.append("█");
                } else {
                    bar.append("░");
                }
            }
            return bar.toString();
        }

        // Foydalanuvchini bloklash
        private void banUser(long adminId, int userNumber) throws TelegramApiException {
            Long userIdToBan = null;

            for (Map.Entry<Long, Integer> entry : userNumberMap.entrySet()) {
                if (entry.getValue() == userNumber) {
                    userIdToBan = entry.getKey();
                    break;
                }
            }

            if (userIdToBan == null) {
                sendText(adminId, "❌ #" + userNumber + " raqamli foydalanuvchi topilmadi!");
                return;
            }

            if (ADMIN_IDS.contains(userIdToBan)) {
                sendText(adminId, "❌ Adminlarni bloklash mumkin emas!");
                return;
            }

            bannedUsers.add(userIdToBan);

            sendText(userIdToBan, "❌ Siz botdan foydalanish huquqidan mahrum qilingansiz!\n\n" +
                    "Agar bu xato deb hisoblasangiz, admin bilan bog'laning.");

            sendText(adminId, "✅ #" + userNumber + " raqamli foydalanuvchi muvaffaqiyatli bloklandi!");
        }

        // Foydalanuvchini blokdan chiqarish
        private void unblockUser(long adminId, int selection) throws TelegramApiException {
            int index = 1;
            Long userIdToUnblock = null;

            for (Long userId : bannedUsers) {
                if (index == selection) {
                    userIdToUnblock = userId;
                    break;
                }
                index++;
            }

            if (userIdToUnblock == null) {
                sendText(adminId, "❌ Foydalanuvchi topilmadi!");
                return;
            }

            bannedUsers.remove(userIdToUnblock);
            int userNumber = userNumberMap.getOrDefault(userIdToUnblock, 0);

            sendText(userIdToUnblock, "✅ Sizning blokingiz olindi! Endi botdan foydalanishingiz mumkin.");
            sendText(adminId, "✅ #" + userNumber + " foydalanuvchi blokdan chiqarildi.");
        }

        // Foydalanuvchini izlash
        private void searchUserByNumber(long adminId, String numberStr) throws TelegramApiException {
            try {
                int userNumber = Integer.parseInt(numberStr.trim());
                Long foundUserId = null;

                for (Map.Entry<Long, Integer> entry : userNumberMap.entrySet()) {
                    if (entry.getValue() == userNumber) {
                        foundUserId = entry.getKey();
                        break;
                    }
                }

                if (foundUserId == null) {
                    sendText(adminId, "❌ #" + userNumber + " raqamli foydalanuvchi topilmadi!");
                    return;
                }

                String fullName = userFullNameMap.getOrDefault(foundUserId, "Noma'lum");
                String phone = phoneMap.getOrDefault(foundUserId, "Noma'lum");
                String username = userUsernameMap.getOrDefault(foundUserId, "Noma'lum");
                int adCount = userAdCountMap.getOrDefault(foundUserId, 0);
                int score = getUserScore(foundUserId);
                boolean isBlocked = bannedUsers.contains(foundUserId);
                int referralsCount = getReferralCount(foundUserId);

                String userInfo = "🔍 *FOYDALANUVCHI MA'LUMOTLARI*\n\n" +
                        "🔢 *Foydalanuvchi raqami:* #" + userNumber + "\n" +
                        "👤 *Ism-familiya:* " + fullName + "\n" +
                        "🔗 *Telegram:* " + username + "\n" +
                        "📞 *Telefon:* " + phone + "\n" +
                        "🆔 *Telegram ID:* " + foundUserId + "\n" +
                        "🏆 *Ballar:* " + score + "\n" +
                        "👥 *Taklif qilganlar:* " + referralsCount + " kishi\n" +
                        "📊 *Bergan reklamalar soni:* " + adCount + " ta\n" +
                        "⛔ *Bloklangan:* " + (isBlocked ? "✅ Ha" : "❌ Yo'q") + "\n" +
                        "⏰ *Oxirgi reklama vaqti:* " +
                        (lastAdTimeMap.containsKey(foundUserId) ?
                                new SimpleDateFormat("dd.MM.yyyy HH:mm").format(lastAdTimeMap.get(foundUserId)) : "Hali yo'q");

                sendText(adminId, userInfo);

            } catch (NumberFormatException e) {
                sendText(adminId, "❌ Iltimos, raqam kiriting! Masalan: 1, 2, 3");
            }
        }

        private void handleResetAllRatings(long adminId) throws TelegramApiException {
            sendText(adminId, "❌ Bu funksiya hozir ishlamaydi!");
        }

        // Media turini tanlash
        private void sendMediaTypeSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📸 Qanday media yuboraysiz?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton photoBtn = new InlineKeyboardButton();
            photoBtn.setText("🖼️ Rasm yuboraman");
            photoBtn.setCallbackData("media_photo");
            rows.add(Collections.singletonList(photoBtn));

            InlineKeyboardButton videoBtn = new InlineKeyboardButton();
            videoBtn.setText("🎥 Video yuboraman");
            videoBtn.setCallbackData("media_video");
            rows.add(Collections.singletonList(videoBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Mushuk sonini tanlash
        private void handleMushukSoni(long chatId, int soni) throws TelegramApiException {
            mushukSoniMap.put(chatId, soni);
            sendMediaTypeSelection(chatId);
        }

        private void sendMushukSoniSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🐱 Sizning mushugingiz nechta?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton btn1 = new InlineKeyboardButton();
            btn1.setText("1 ta mushuk");
            btn1.setCallbackData("mushuk_1");
            rows.add(Collections.singletonList(btn1));

            InlineKeyboardButton btn2 = new InlineKeyboardButton();
            btn2.setText("2 ta mushuk");
            btn2.setCallbackData("mushuk_2");
            rows.add(Collections.singletonList(btn2));

            InlineKeyboardButton btn3 = new InlineKeyboardButton();
            btn3.setText("3 ta mushuk");
            btn3.setCallbackData("mushuk_3");
            rows.add(Collections.singletonList(btn3));

            InlineKeyboardButton btn4 = new InlineKeyboardButton();
            btn4.setText("4 ta mushuk");
            btn4.setCallbackData("mushuk_4");
            rows.add(Collections.singletonList(btn4));

            InlineKeyboardButton btn5 = new InlineKeyboardButton();
            btn5.setText("5 ta mushuk");
            btn5.setCallbackData("mushuk_5");
            rows.add(Collections.singletonList(btn5));

            InlineKeyboardButton btn6 = new InlineKeyboardButton();
            btn6.setText("+5 ta mushuk");
            btn6.setCallbackData("mushuk_kop");
            rows.add(Collections.singletonList(btn6));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Platforma tanlash
        private void sendPlatformaSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📱 Qaysi platformaga reklama qilmoqchisiz?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton telegramBtn = new InlineKeyboardButton();
            telegramBtn.setText("📢 Telegramga reklama qilmoqchiman");
            telegramBtn.setCallbackData("platforma_telegram");
            rows.add(Collections.singletonList(telegramBtn));

            InlineKeyboardButton instagramBtn = new InlineKeyboardButton();
            instagramBtn.setText("📷 Instagramga reklama qilmoqchiman");
            instagramBtn.setCallbackData("platforma_instagram");
            rows.add(Collections.singletonList(instagramBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("adtype_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Yosh tanlash
        private void sendAgeSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🎂 Yoshini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // 5 ta qator, har qatorda 4 ta tugma
            for (int i = 0; i < ages.size(); i += 4) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                for (int j = i; j < Math.min(i + 4, ages.size()); j++) {
                    InlineKeyboardButton btn = new InlineKeyboardButton();
                    String age = ages.get(j);
                    btn.setText(age);

                    // To'g'ri callbackData format
                    String callbackData = "age_" + age.toLowerCase()
                            .replace(" ", "_")
                            .replace("+", "")
                            .replace(",", "")
                            .replace(" ", "_")
                            .replace(".", "");

                    // Alohida holatlar
                    if (age.equals("1,5 yosh")) {
                        callbackData = "age_15_yosh";
                    } else if (age.equals("2,5 yosh")) {
                        callbackData = "age_25_yosh";
                    } else if (age.equals("3,5 yosh")) {
                        callbackData = "age_35_yosh";
                    } else if (age.equals("4,5 yosh")) {
                        callbackData = "age_45_yosh";
                    }

                    btn.setCallbackData(callbackData);
                    row.add(btn);
                }
                if (!row.isEmpty()) {
                    rows.add(row);
                }
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Sog'lik tanlash
        private void sendHealthSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("❤️ Sog'lig'ini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("Sog'lom");
            b1.setCallbackData("health_soglom");

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("Kasal");
            b2.setCallbackData("health_kasal");

            rows.add(Arrays.asList(b1, b2));
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Jins tanlash
        private void sendGenderSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👤 Jinsini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("♀️ Qiz bola");
            b1.setCallbackData("gender_qiz");
            rows.add(Collections.singletonList(b1));

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("♂️ O'g'il bola");
            b2.setCallbackData("gender_ogil");
            rows.add(Collections.singletonList(b2));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Sterilization tanlash
        private void sendSterilizationSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🧬 Nasl olish mumkinmi?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("✅ Nasil olsa buladi ");
            b1.setCallbackData("sterilization_yes");
            rows.add(Collections.singletonList(b1));

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("❌ Nasl olsa bulmaydi");
            b2.setCallbackData("sterilization_no");
            rows.add(Collections.singletonList(b2));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Breed tanlash
        private void sendBreedSelectionWithCustom(long chatId, int page) throws TelegramApiException {
            List<String> currentBreeds = breedPages.get(page);

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🐱 Mushuk zotini tanlang yoki o'zingiz yozing:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = 0; i < currentBreeds.size(); i++) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(currentBreeds.get(i));
                btn.setCallbackData("breed_select_" + page + "_" + i);
                rows.add(Collections.singletonList(btn));
            }

            InlineKeyboardButton customBreedBtn = new InlineKeyboardButton();
            customBreedBtn.setText("✏️ Yozuvda kiritaman");
            customBreedBtn.setCallbackData("breed_custom");
            rows.add(Collections.singletonList(customBreedBtn));

            List<InlineKeyboardButton> navRow = new ArrayList<>();

            if (page > 1) {
                InlineKeyboardButton backBtn = new InlineKeyboardButton();
                backBtn.setText("⬅️ Orqaga");
                backBtn.setCallbackData("breed_page_" + (page - 1));
                navRow.add(backBtn);
            }

            if (page < breedPages.size()) {
                InlineKeyboardButton nextBtn = new InlineKeyboardButton();
                nextBtn.setText("Boshqa ➡️");
                nextBtn.setCallbackData("breed_page_" + (page + 1));
                navRow.add(nextBtn);
            }

            if (!navRow.isEmpty()) {
                rows.add(navRow);
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Viloyat tanlash
        private void sendViloyatSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📍 Manzilingizni tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (String viloyat : viloyatlar) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(viloyat);
                btn.setCallbackData("viloyat_" + viloyat.toLowerCase().replace(" ", "_").replace("'", ""));
                rows.add(Collections.singletonList(btn));
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Preview yuborish
        private void sendPreview(long chatId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);
            String mediaType = mediaTypeMap.getOrDefault(chatId, "photo");

            if (userPhotos == null || userPhotos.isEmpty()) {
                sendText(chatId, "❌ Xatolik: Media topilmadi.");
                return;
            }

            if ("video".equals(mediaType) && userPhotos.size() > 0 && userPhotos.get(0).startsWith("video:")) {
                String videoFileId = userPhotos.get(0).substring(6);
                SendVideo video = new SendVideo();
                video.setChatId(String.valueOf(chatId));
                video.setVideo(new InputFile(videoFileId));
                video.setCaption(buildPreviewCaption(chatId));

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton confirm = new InlineKeyboardButton();
                confirm.setText("✅ Tasdiqlash");
                confirm.setCallbackData("preview_confirm");

                InlineKeyboardButton back = new InlineKeyboardButton();
                back.setText("↩️ Orqaga");
                back.setCallbackData("preview_back");

                markup.setKeyboard(Collections.singletonList(Arrays.asList(confirm, back)));
                video.setReplyMarkup(markup);

                execute(video);
            }
            else {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(chatId));
                photo.setPhoto(new InputFile(userPhotos.get(0)));
                photo.setCaption(buildPreviewCaption(chatId));

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton confirm = new InlineKeyboardButton();
                confirm.setText("✅ Tasdiqlash");
                confirm.setCallbackData("preview_confirm");

                InlineKeyboardButton back = new InlineKeyboardButton();
                back.setText("↩️ Orqaga");
                back.setCallbackData("preview_back");

                markup.setKeyboard(Collections.singletonList(Arrays.asList(confirm, back)));
                photo.setReplyMarkup(markup);

                execute(photo);
            }
        }

        // Preview caption qurish
        private String buildPreviewCaption(long chatId) {
            StringBuilder sb = new StringBuilder();
            String adType = adTypeMap.getOrDefault(chatId, "");

            if ("vyazka".equals(adType)) {
                sb.append("💝 VYAZKA - E'lon ma'lumotlari:\n\n");
            } else if ("sotish".equals(adType)) {
                sb.append("📋 SOTISH - E'lon ma'lumotlari:\n\n");
            } else {
                sb.append("🎁 HADIYA - E'lon ma'lumotlari:\n\n");
            }

            sb.append("📍 Manzil: ").append(manzilMap.getOrDefault(chatId, "—")).append("\n");

            if ("sotish".equals(adType) || "vyazka".equals(adType)) {
                String valyuta = valyutaMap.getOrDefault(chatId, "so'm");
                String narxBelgisi = "so'm".equals(valyuta) ? " so'm" : " $";
                sb.append("💰 Narx: ").append(priceMap.getOrDefault(chatId, "—")).append(narxBelgisi).append("\n");
            }

            sb.append("📞 Telefon: ").append(phoneMap.getOrDefault(chatId, "—")).append("\n");

            if (!"hadiya".equals(adType)) {
                sb.append("🐱 Zot: ").append(breedMap.getOrDefault(chatId, "—")).append("\n");
                sb.append("🎂 Yosh: ").append(ageMap.getOrDefault(chatId, "—")).append("\n");
                sb.append("👤 Jins: ").append(genderMap.getOrDefault(chatId, "—")).append("\n");

                if ("sotish".equals(adType)) {
                    sb.append("❤️ Sog'lig'i: ").append(healthMap.getOrDefault(chatId, "—")).append("\n");
                }

                int mushukSoni = mushukSoniMap.getOrDefault(chatId, 1);
                sb.append("🐾 Mushuklar soni: ").append(mushukSoni).append(" ta\n");
                sb.append("🧬 Nasl olish: ").append(sterilizationMap.getOrDefault(chatId, "—")).append("\n");
            }

            sb.append("\nMa'lumotlaringiz to'g'rimi?");
            return sb.toString();
        }

        // Davom etish tugmasi
        private void sendContinueButton(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("Agar boshqa rasm yoki video jo'natmaydigan bo'lsangiz davom etish tugmasini bosing!");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton continueBtn = new InlineKeyboardButton();
            continueBtn.setText("➡️ Davom etish");
            continueBtn.setCallbackData("continue_process");

            rows.add(Collections.singletonList(continueBtn));
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Davom etishni qayta ishlash
        private void handleContinueProcess(long chatId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);

            if (userPhotos == null || userPhotos.isEmpty()) {
                sendText(chatId, "❌ Iltimos, kamida 1 ta rasm yoki video yuboring!");
                return;
            }

            sendViloyatSelection(chatId);
        }

        // Narxlar ro'yxati
        private void sendPriceList(long chatId) throws TelegramApiException {
            String priceText = "🐱 *MUSHUK REKLAMA NARXLARI*\n\n" +
                    "❕ Iltimos oxirigacha diqqat bilan o'qib tanishib chiqing.\n\n" +
                    "📢 *Telegram Reklama Narxlari:*\n" +
                    "• [Telegram Kanal](https://t.me/uzbek_cats) - 35,000 so'm\n" +
                    "   _(mushukcha sotilguncha turadi)_\n\n" +
                    "📷 *Instagram Reklama Narxlari:*\n" +
                    "• [Instagram Story](https://instagram.com/zayd.catlover) - 40,000 so'm\n\n" +
                    "👤 *Shaxsiy Telegram Story:*\n" +
                    "• [Shaxsiy Telegram](https://t.me/zayd_catlover) - 15,000 so'm\n\n" +
                    "💝 *Mushukgimga juft topmoqchiman (viyazka):*\n" +
                    "• [Telegram Kanal](https://t.me/uzbek_cats) - 100,000 so'm\n" +
                    "   _(o'chib ketmaydi, doim turadi)_\n\n" +
                    "• [Instagram Story](https://instagram.com/zayd.catlover) - 50,000 so'm\n" +
                    "   _(aktual qo'yiladi, umrbod turadi)_\n\n" +
                    "• [Shaxsiy Telegram](https://t.me/zayd_catlover) - 20,000 so'm\n" +
                    "   _(24 soat turadi)_\n\n" +
                    "⚠️ *Eslatma:* Yuqoridagi narxlar faqat 1 ta mushuk reklamasi uchun.\n\n" +
                    "💳 *To'lov Ma'lumotlari:*\n" +
                    "• Karta raqam: `5614 6816 2628 0956`\n" +
                    "• Karta egasi: Xalilov.A\n\n" +
                    "🙋‍♂️ *Admin bilan bog'lanish:*\n" +
                    "• [Telegram](https://t.me/zayd_catlover)\n" +
                    "• 📞 +998934938181";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(priceText);
            msg.setParseMode("Markdown");
            msg.disableWebPagePreview();

            execute(msg);
        }

        // Telefon raqamni tekshirish
        private boolean isValidPhoneNumber(String phone) {
            String regex = "^\\+998\\s\\d{2}\\s\\d{3}\\s\\d{2}\\s\\d{2}$";
            if (!phone.matches(regex)) {
                String digitsOnly = phone.replaceAll("[^0-9]", "");
                return digitsOnly.length() >= 12 && digitsOnly.startsWith("998") && digitsOnly.substring(3).length() == 9;
            }
            return true;
        }

        // Reklama turini tanlash
        private void sendAdTypeSelection(long chatId) throws TelegramApiException {
            if (userHasPendingAdMap.getOrDefault(chatId, false)) {
                Date lastAdTime = lastAdTimeMap.get(chatId);
                if (lastAdTime != null) {
                    long hoursPassed = (new Date().getTime() - lastAdTime.getTime()) / (60 * 60 * 1000);
                    if (hoursPassed < 24) {
                        sendText(chatId, "⏳ Iltimos, birinchi reklamangiz admin tomonidan tasdiqlangandan keyin ikkinchi reklamani joylang!\n\n" +
                                "Hozir sizda tasdiqlanmagan reklama mavjud. Admin tez orada uni ko'rib chiqadi.");
                        return;
                    }
                }
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🎯 Quyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("💰 Mushukimni Sotish uchun quymoqchiman");
            b1.setCallbackData("adtype_sotish");
            rows.add(Collections.singletonList(b1));

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("🎁 Mushukgimni Hadiyaga bermoqchiman");
            b2.setCallbackData("adtype_hadiya");
            rows.add(Collections.singletonList(b2));

            InlineKeyboardButton b3 = new InlineKeyboardButton();
            b3.setText("\uD83D\uDC8D Mushukgimni Viyazkaga(kupaytirish uchun elon)  ");
            b3.setCallbackData("adtype_vyazka");
            rows.add(Collections.singletonList(b3));

            InlineKeyboardButton b4 = new InlineKeyboardButton();
            b4.setText("\uD83D\uDE91 Mushukgim kasal tekinga bermoqchian");
            b4.setCallbackData("menu_yordam");
            rows.add(Collections.singletonList(b4));

            InlineKeyboardButton b5 = new InlineKeyboardButton();
            b5.setText("💰 Narxlar bilan tanishib chiqmoqchiman");
            b5.setCallbackData("menu_narx");
            rows.add(Collections.singletonList(b5));

            InlineKeyboardButton b6 = new InlineKeyboardButton();
            b6.setText("↩️ Asosiy menyuga qaytish");
            b6.setCallbackData("adtype_back");
            rows.add(Collections.singletonList(b6));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Yordam menyusi
        private void sendYordamMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("\uD83D\uDE91 Qanday yordam kerak?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("🐱 Mushukimni onasi yuq hadiyaga beraman");
            b1.setCallbackData("yordam_onasiz");
            rows.add(Collections.singletonList(b1));

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("🏥 Mushukgim kasal yordam kerak");
            b2.setCallbackData("yordam_kasal");
            rows.add(Collections.singletonList(b2));

            InlineKeyboardButton b3 = new InlineKeyboardButton();
            b3.setText("🎁 Kasal Mushuk Hadiyaga beraman");
            b3.setCallbackData("yordam_kasal_hadiya");
            rows.add(Collections.singletonList(b3));

            InlineKeyboardButton b4 = new InlineKeyboardButton();
            b4.setText("↩️ Orqaga");
            b4.setCallbackData("yordam_back");
            rows.add(Collections.singletonList(b4));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Yordam turlari
        private void handleYordamOnasiz(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "yordam_onasiz");
            photosMap.put(chatId, new ArrayList<>());

            String message = "🐱 **ONASIZ MUSHUK**\n\n" +
                    "Agar sizda onasiz mushuk bolalar bo'lsa va ularga yordam kerak bo'lsa, iltimos quyidagilarni bajaring:\n\n" +
                    "📸 Mushukchalarning 1-3 ta rasmini yoki 10 soniyagacha videolarini yuboring\n\n" +
                    "📍 Manzilingizni va telefon raqamingizni kiritasiz\n\n" +
                    "✅ So'rovingizni tasdiqlaganingizdan so'ng adminlar tekshirib kanalga joylaydi";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(message);
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("✅ Ko'rib chiqdim va rasm yuborishni boshlayman");
            b1.setCallbackData("yordam_confirm");

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("↩️ Orqaga");
            b2.setCallbackData("yordam_cancel");

            rows.add(Collections.singletonList(b1));
            rows.add(Collections.singletonList(b2));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        private void handleYordamKasal(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "yordam_kasal");
            photosMap.put(chatId, new ArrayList<>());

            String message = "🏥 **KASAL MUSHUK**\n\n" +
                    "Agar sizda kasal mushuk bo'lsa va unga davolash uchun yordam kerak bo'lsa, iltimos quyidagilarni bajaring:\n\n" +
                    "📸 Kasal mushukning 1-3 ta rasmini yoki 10 soniyagacha videolarini yuboring\n\n" +
                    "📍 Manzilingizni va telefon raqamingizni kiritasiz\n\n" +
                    "✅ So'rovingizni tasdiqlaganingizdan so'ng adminlar tekshirib kanalga joylaydi";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(message);
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("✅ Ko'rib chiqdim va rasm yuborishni boshlayman");
            b1.setCallbackData("yordam_confirm");

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("↩️ Orqaga");
            b2.setCallbackData("yordam_cancel");

            rows.add(Collections.singletonList(b1));
            rows.add(Collections.singletonList(b2));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        private void handleYordamKasalHadiya(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "yordam_kasal_hadiya");
            photosMap.put(chatId, new ArrayList<>());

            String message = "🎁 **KASAL MUSHUK HADIYAGA**\n\n" +
                    "Agar sizda kasal mushuk bo'lsa va uni boqolmasangiz, boshqalarga hadiya qilmoqchi bo'lsangiz, iltimos quyidagilarni bajaring:\n\n" +
                    "📸 Kasal mushukning 1-3 ta rasmini yoki 10 soniyagacha videolarini yuboring\n\n" +
                    "📍 Manzilingizni va telefon raqamingizni kiritasiz\n\n" +
                    "✅ So'rovingizni tasdiqlaganingizdan so'ng adminlar tekshirib kanalga joylaydi";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(message);
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("✅ Ko'rib chiqdim va rasm yuborishni boshlayman");
            b1.setCallbackData("yordam_confirm");

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("↩️ Orqaga");
            b2.setCallbackData("yordam_cancel");

            rows.add(Collections.singletonList(b1));
            rows.add(Collections.singletonList(b2));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        private void handleYordamConfirm(long chatId) throws TelegramApiException {
            String state = stateMap.get(chatId);
            stateMap.put(chatId, state + "_photo");
            sendText(chatId, "📸 Endi rasm yuboring (1-3 ta rasm yoki 10 soniyagacha video):");
        }

        // Yordam uchun viloyat tanlash
        private void sendYordamViloyatSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📍 Manzilingizni tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (String viloyat : viloyatlar) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(viloyat);
                btn.setCallbackData("yordam_viloyat_" + viloyat.toLowerCase().replace(" ", "_").replace("'", ""));
                rows.add(Collections.singletonList(btn));
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Yordam preview
        private void sendYordamPreview(long chatId, String userInfo) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);
            String state = stateMap.get(chatId);

            String caption = "";
            String finalText = "";

            if (state.equals("yordam_onasiz")) {
                caption = "🐱Mushukgimni onasi yuq yozrdam kerak \n\n";
                finalText = "mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing";
            } else if (state.equals("yordam_kasal")) {
                caption = "🏥 Mushukgim kanal yordam kerak\n\n";
                finalText = "Kasal mushukka yordam kerak. Iltimos, mushukni davolash uchun yordam bering!";
            } else if (state.equals("yordam_kasal_hadiya")) {
                caption = "🎁 Mushukgim kasal tekinga beraman \n\n";
                finalText = "mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing";
            }

            caption += "📍 Ma'lumot: " + userInfo + "\n\n";
            caption += finalText + "\n\nMa'lumotlaringiz to'g'rimi?";

            if (userPhotos != null && !userPhotos.isEmpty()) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(chatId));
                photo.setPhoto(new InputFile(userPhotos.get(0)));
                photo.setCaption(caption);

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton confirm = new InlineKeyboardButton();
                confirm.setText("✅ Tasdiqlash");
                confirm.setCallbackData("yordam_final_confirm");

                InlineKeyboardButton back = new InlineKeyboardButton();
                back.setText("↩️ Orqaga");
                back.setCallbackData("yordam_cancel");

                markup.setKeyboard(Arrays.asList(
                        Collections.singletonList(confirm),
                        Collections.singletonList(back)
                ));
                photo.setReplyMarkup(markup);

                execute(photo);
            } else {
                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(chatId));
                msg.setText(caption);

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton confirm = new InlineKeyboardButton();
                confirm.setText("✅ Tasdiqlash");
                confirm.setCallbackData("yordam_final_confirm");

                InlineKeyboardButton back = new InlineKeyboardButton();
                back.setText("↩️ Orqaga");
                back.setCallbackData("yordam_cancel");

                markup.setKeyboard(Arrays.asList(
                        Collections.singletonList(confirm),
                        Collections.singletonList(back)
                ));
                msg.setReplyMarkup(markup);

                execute(msg);
            }
        }

        // Admin statistikasi
        private void sendAdminStatisticsMenu(long chatId) throws TelegramApiException {
            if (!ADMIN_IDS.contains(chatId)) {
                sendText(chatId, "❌ Siz admin emassiz!");
                return;
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📊 Statistika bo'limi:\n\nQuyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton hadiyaBtn = new InlineKeyboardButton();
            hadiyaBtn.setText("🎁 Hadiyaga berilgan mushuklar");
            hadiyaBtn.setCallbackData("stat_hadiya");
            rows.add(Collections.singletonList(hadiyaBtn));

            InlineKeyboardButton sotishBtn = new InlineKeyboardButton();
            sotishBtn.setText("💰 Sotilgan mushuklar");
            sotishBtn.setCallbackData("stat_sotish");
            rows.add(Collections.singletonList(sotishBtn));

            InlineKeyboardButton vyazkaBtn = new InlineKeyboardButton();
            vyazkaBtn.setText("💝 Vyazkaga berilgan mushuklar");
            vyazkaBtn.setCallbackData("stat_vyazka");
            rows.add(Collections.singletonList(vyazkaBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("admin_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Statistikani ko'rsatish
        private void showStatistics(long chatId, String adType) throws TelegramApiException {
            List<AdRecord> records = statisticsMap.getOrDefault(adType, new ArrayList<>());

            String typeName = "";
            String emoji = "";

            switch (adType) {
                case "hadiya":
                    typeName = "Hadiyaga berilgan";
                    emoji = "🎁";
                    break;
                case "sotish":
                    typeName = "Sotilgan";
                    emoji = "💰";
                    break;
                case "vyazka":
                    typeName = "Vyazkaga berilgan";
                    emoji = "💝";
                    break;
            }

            StringBuilder statsText = new StringBuilder();
            statsText.append(emoji).append(" *").append(typeName).append(" mushuklar:* ").append(records.size()).append(" ta\n\n");

            if (records.isEmpty()) {
                statsText.append("Hozircha hech qanday e'lon yo'q\n");
            } else {
                for (int i = 0; i < records.size(); i++) {
                    AdRecord record = records.get(i);
                    String username = record.username != null ? record.username : "ID: " + record.userId;
                    statsText.append("*").append(i + 1).append(".* #").append(record.adNumber)
                            .append(" - Foydalanuvchi ID: ").append(record.userId)
                            .append("\n   👤 ").append(username)
                            .append("\n   📊 Reklama raqami: ").append(record.adNumber)
                            .append("\n   ⏰ ").append(record.getFormattedDate())
                            .append("\n\n");
                }
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(statsText.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("stat_back");

            markup.setKeyboard(Collections.singletonList(Collections.singletonList(backBtn)));
            msg.setReplyMarkup(markup);

            execute(msg);
        }

        // Admin breed tanlash
        private void handleAdminSetBreed(long adminId, String data) throws TelegramApiException {
            Long userId = adminEditUserIdMap.get(adminId);
            if (userId == null) return;

            String breed = data.replace("admin_set_breed_", "").replace("_", " ");
            breedMap.put(userId, breed);

            sendText(adminId, "✅ Zot o'zgartirildi: " + breed);
            sendAdminEditMenu(adminId, userId);
        }

        // Admin field edit
        private void handleAdminEditField(long adminId, String data) throws TelegramApiException {
            String field = data.substring("admin_edit_field_".length());
            Long userId = adminEditUserIdMap.get(adminId);

            if (userId != null) {
                stateMap.put(adminId, "admin_edit_" + field);

                switch (field) {
                    case "manzil":
                        sendText(adminId, "📍 Yangi manzilni kiriting:");
                        break;
                    case "phone":
                        sendText(adminId, "📞 Yangi telefon raqamini kiriting:");
                        break;
                    case "price":
                        sendText(adminId, "💰 Yangi narxni kiriting:");
                        break;
                }
            }
        }

        // Admin breed edit
        private void handleAdminEditBreed(long adminId) throws TelegramApiException {
            sendAdminBreedSelection(adminId);
        }

        // Admin edit confirm
        private void handleAdminEditConfirm(long adminId) throws TelegramApiException {
            Long userId = adminEditUserIdMap.get(adminId);
            if (userId == null) return;

            int adNumber = userAdCountMap.getOrDefault(userId, 0) + 1;
            userAdCountMap.put(userId, adNumber);
            postToChannel(userId, adNumber);
            sendText(userId, "✅ E'loningiz kanalga joylandi!");
            sendText(adminId, "✅ E'lon o'zgartirildi va kanalga joylandi!");

            deleteAdminMessages(userId);
            adminEditUserIdMap.remove(adminId);

            userHasPendingAdMap.put(userId, false);
        }

        // Admin breed selection
        private void sendAdminBreedSelection(long adminId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText("🐱 Yangi zotni tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            int itemsPerPage = 8;
            for (int i = 0; i < allBreeds.size(); i++) {
                if (i % itemsPerPage == 0 && i > 0) {
                    rows.add(new ArrayList<>());
                }

                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(allBreeds.get(i));
                btn.setCallbackData("admin_set_breed_" + allBreeds.get(i).toLowerCase().replace(" ", "_"));

                if (rows.isEmpty() || rows.get(rows.size()-1).size() >= 2) {
                    rows.add(new ArrayList<>());
                }
                rows.get(rows.size()-1).add(btn);
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Admin edit menu
        private void sendAdminEditMenu(long adminId, long userId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText("✏️ O'zgartirmoqchi bo'lgan maydonni tanlang:\n\n" + buildAdminPreviewCaption(userId));

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            String username = userUsernameMap.getOrDefault(userId, "user_" + userId);
            InlineKeyboardButton contactBtn = new InlineKeyboardButton();
            contactBtn.setText("👥 Obunachi bilan aloqa");
            contactBtn.setUrl("https://t.me/" + username.replace("@", ""));
            rows.add(Collections.singletonList(contactBtn));

            InlineKeyboardButton breedBtn = new InlineKeyboardButton();
            breedBtn.setText("✏️ Zotni o'zgartirish");
            breedBtn.setCallbackData("admin_edit_breed");
            rows.add(Collections.singletonList(breedBtn));

            InlineKeyboardButton manzilBtn = new InlineKeyboardButton();
            manzilBtn.setText("📍 Manzilni o'zgartirish");
            manzilBtn.setCallbackData("admin_edit_field_manzil");
            rows.add(Collections.singletonList(manzilBtn));

            InlineKeyboardButton phoneBtn = new InlineKeyboardButton();
            phoneBtn.setText("📞 Telefon raqamini o'zgartirish");
            phoneBtn.setCallbackData("admin_edit_field_phone");
            rows.add(Collections.singletonList(phoneBtn));

            InlineKeyboardButton priceBtn = new InlineKeyboardButton();
            priceBtn.setText("💰 Narxni o'zgartirish");
            priceBtn.setCallbackData("admin_edit_field_price");
            rows.add(Collections.singletonList(priceBtn));

            InlineKeyboardButton confirmBtn = new InlineKeyboardButton();
            confirmBtn.setText("✅ Tasdiqlash va kanalga joylash");
            confirmBtn.setCallbackData("admin_edit_confirm");

            InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
            cancelBtn.setText("❌ Bekor qilish");
            cancelBtn.setCallbackData("admin_edit_cancel");

            rows.add(Arrays.asList(confirmBtn, cancelBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Admin preview caption
        private String buildAdminPreviewCaption(long userId) {
            return "📋 Joriy ma'lumotlar:\n\n" +
                    "🐱 Zot: " + breedMap.getOrDefault(userId, "—") + "\n" +
                    "🎂 Yosh: " + ageMap.getOrDefault(userId, "—") + "\n" +
                    "❤️ Sog'lig'i: " + healthMap.getOrDefault(userId, "—") + "\n" +
                    "🧬 Nasl olish: " + sterilizationMap.getOrDefault(userId, "—") + "\n" +
                    "👤 Jins: " + genderMap.getOrDefault(userId, "—") + "\n" +
                    "📍 Manzil: " + manzilMap.getOrDefault(userId, "—") + "\n" +
                    "📞 Telefon: " + phoneMap.getOrDefault(userId, "—") + "\n" +
                    "💰 Narx: " + priceMap.getOrDefault(userId, "—");
        }

        // Konkurs menyusi
        private void sendKonkursMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🏆 Konkurs bo'limiga xush kelibsiz!\n\nQuyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton mukofotBtn = new InlineKeyboardButton();
            mukofotBtn.setText("🎁 Mukofot");
            mukofotBtn.setCallbackData("konkurs_mukofot");
            rows.add(Collections.singletonList(mukofotBtn));

            InlineKeyboardButton retingBtn = new InlineKeyboardButton();
            retingBtn.setText("🏆 Reting");
            retingBtn.setCallbackData("konkurs_reting");
            rows.add(Collections.singletonList(retingBtn));

            InlineKeyboardButton shartlarBtn = new InlineKeyboardButton();
            shartlarBtn.setText("📋 Shartlar");
            shartlarBtn.setCallbackData("konkurs_shartlar");
            rows.add(Collections.singletonList(shartlarBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("konkurs_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Konkurs mukofot
        private void sendKonkursMukofot(long chatId) throws TelegramApiException {
            try {
                if (currentKonkursImageUrl != null && !currentKonkursImageUrl.isEmpty()) {
                    SendPhoto photo = new SendPhoto();
                    photo.setChatId(String.valueOf(chatId));
                    photo.setPhoto(new InputFile(currentKonkursImageUrl));
                    photo.setCaption(currentKonkursText);
                    photo.setParseMode("Markdown");
                    execute(photo);
                } else {
                    sendText(chatId, currentKonkursText);
                }
            } catch (Exception e) {
                System.out.println("❌ Konkurs rasm yuborishda xatolik: " + e.getMessage());
                sendText(chatId, "📷 " + currentKonkursText);
            }
        }

        // Konkurs o'zgartirish
        private void sendKonkursChangeMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🏆 Konkursni o'zgartirish:\n\nQuyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton imageBtn = new InlineKeyboardButton();
            imageBtn.setText("🖼️ Faqat rasm o'zgartirish");
            imageBtn.setCallbackData("admin_konkurs_image_only");
            rows.add(Collections.singletonList(imageBtn));

            InlineKeyboardButton textBtn = new InlineKeyboardButton();
            textBtn.setText("📝 Faqat matn o'zgartirish");
            textBtn.setCallbackData("admin_konkurs_text_only");
            rows.add(Collections.singletonList(textBtn));

            InlineKeyboardButton bothBtn = new InlineKeyboardButton();
            bothBtn.setText("🔄 Hammasini o'zgartirish");
            bothBtn.setCallbackData("admin_konkurs_both");
            rows.add(Collections.singletonList(bothBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("admin_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Konkurs image only
        private void handleAdminKonkursImageOnly(long adminId) throws TelegramApiException {
            stateMap.put(adminId, "admin_await_konkurs_image_only");
            sendText(adminId, "🖼️ Iltimos, yangi konkurs rasmini yuboring:");
        }

        // Konkurs text only
        private void handleAdminKonkursTextOnly(long adminId) throws TelegramApiException {
            stateMap.put(adminId, "admin_await_konkurs_text_only");
            sendText(adminId, "📝 Iltimos, yangi konkurs matnini yuboring:");
        }

        // Konkurs both
        private void handleAdminKonkursBoth(long adminId) throws TelegramApiException {
            stateMap.put(adminId, "admin_await_konkurs_image_both");
            sendText(adminId, "🖼️ Iltimos, yangi konkurs rasmini yuboring:");
        }

        // About menyusi
        private void sendAboutMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("ℹ️ Bot haqida:\n\n" +
                    "Ushbu bot orqali siz mushuklar haqida e'lon berishingiz mumkin.\n\n" +
                    "Agar sizda shikoyat yoki takliflar bo'lsa, quyidagi tugmani bosing:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton feedbackBtn = new InlineKeyboardButton();
            feedbackBtn.setText("✍️ Shikoyat/Taklif yuborish");
            feedbackBtn.setCallbackData("about_send_feedback");
            rows.add(Collections.singletonList(feedbackBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("about_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Feedback yuborish
        private void sendFeedbackToOwner(long userId, String feedback) throws TelegramApiException {
            String userInfo = userUsernameMap.containsKey(userId) ?
                    userUsernameMap.get(userId) : "ID: " + userId;

            String message = "📝 YANGI FEEDBACK\n\n" +
                    "👤 Foydalanuvchi: " + userInfo + "\n" +
                    "🆔 User ID: " + userId + "\n" +
                    "📄 Xabar:\n" + feedback;

            sendText(7038296036L, message);
        }

        // Eski e'lonlarni tozalash
        private void cleanupExpiredAds() {
            System.out.println("🔄 Eski e'lonlarni tozalash...");

            int removedCount = 0;
            for (List<UserAd> userAds : userAdsMap.values()) {
                Iterator<UserAd> iterator = userAds.iterator();
                while (iterator.hasNext()) {
                    UserAd ad = iterator.next();
                    if (ad.isExpired()) {
                        iterator.remove();
                        removedCount++;
                    }
                }
            }

            if (removedCount > 0) {
                System.out.println("✅ " + removedCount + " ta eski e'lon o'chirildi");
            }
        }

        // Tozalash timer'ini ishga tushirish
        private void startCleanupTimer() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    cleanupExpiredAds();
                }
            }, 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);
        }

        // Foydalanuvchi e'lonini saqlash
        private void saveUserAd(long userId, int adNumber) {
            if (!userAdsMap.containsKey(userId)) {
                userAdsMap.put(userId, new ArrayList<>());
            }

            long adId = System.currentTimeMillis();

            UserAd userAd = new UserAd(
                    adId,
                    adTypeMap.getOrDefault(userId, ""),
                    new ArrayList<>(photosMap.getOrDefault(userId, new ArrayList<>())),
                    breedMap.getOrDefault(userId, ""),
                    ageMap.getOrDefault(userId, ""),
                    genderMap.getOrDefault(userId, ""),
                    healthMap.getOrDefault(userId, ""),
                    sterilizationMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""),
                    phoneMap.getOrDefault(userId, ""),
                    priceMap.getOrDefault(userId, ""),
                    valyutaMap.getOrDefault(userId, "so'm"),
                    adNumber
            );

            userAdsMap.get(userId).add(userAd);
            System.out.println("✅ E'lon saqlandi. User: #" + userNumberMap.getOrDefault(userId, 0) +
                    ", Reklama #" + adNumber + ", E'lonlar soni: " + userAdsMap.get(userId).size());
        }

        // Reklama paneli
        private void sendAdPanel(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("📢 Reklama Panel\n\n" +
                    "Bot foydalanuvchilariga reklama yuborish:\n\n" +
                    "📍 Joriy holat:\n" +
                    "• Foydalanuvchilar: " + userUsernameMap.size() + " ta\n" +
                    "• Oxirgi reklama: " + getLastAdTime() + "\n\n" +
                    "Quyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton newAdBtn = new InlineKeyboardButton();
            newAdBtn.setText("🆕 Yangi Reklama");
            newAdBtn.setCallbackData("ad_new");
            rows.add(Collections.singletonList(newAdBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Asosiy Menyu");
            backBtn.setCallbackData("menu_main");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Oxirgi reklama vaqti
        private String getLastAdTime() {
            return "Hali reklama yo'q";
        }

        // Yangi reklama boshlash
        private void startNewAd(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "ad_await_text");
            sendText(chatId, "📝 Reklama matnini kiriting:\n\n" +
                    "Matn formati:\n" +
                    "• Maksimum 1000 belgi\n" +
                    "• HTML formatida bo'lishi mumkin\n" +
                    "• Havolalar qo'shishingiz mumkin\n\n" +
                    "Namuna:\n" +
                    "🎉 <b>Yangi chegirmalar!</b>\n" +
                    "Mushuklar uchun aksessuarlar 50% gacha chegirma!\n" +
                    "👉 @mystore");
        }

        // Reklama rasmini so'rash
        private void askForAdPhoto(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "ad_await_photo");
            sendText(chatId, "🖼️ Endi reklama rasmini yuboring:\n\n" +
                    "Talablar:\n" +
                    "• Rasm aniq va sifatli bo'lsin\n" +
                    "• Format: JPEG, PNG\n" +
                    "• Hajm: 5MB dan oshmasin\n\n" +
                    "Agar rasm kerak bo'lmasa, \"Rasmsiz yuborish\" tugmasini bosing.");

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("Yoki rasmsiz davom eting:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton noPhotoBtn = new InlineKeyboardButton();
            noPhotoBtn.setText("📝 Rasmsiz yuborish");
            noPhotoBtn.setCallbackData("ad_no_photo");

            rows.add(Collections.singletonList(noPhotoBtn));
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Reklama preview
        private void showAdPreview(long chatId) throws TelegramApiException {
            String adText = adTextMap.get(chatId);
            String photoId = adPhotoMap.get(chatId);

            if (photoId != null && !photoId.isEmpty()) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(chatId));
                photo.setPhoto(new InputFile(photoId));
                photo.setCaption(adText);
                photo.setParseMode("HTML");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                InlineKeyboardButton sendBtn = new InlineKeyboardButton();
                sendBtn.setText("✅ Reklamani Yuborish");
                sendBtn.setCallbackData("ad_send");

                InlineKeyboardButton editBtn = new InlineKeyboardButton();
                editBtn.setText("✏️ Tahrirlash");
                editBtn.setCallbackData("ad_edit");

                InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
                cancelBtn.setText("❌ Bekor qilish");
                cancelBtn.setCallbackData("ad_cancel");

                rows.add(Collections.singletonList(sendBtn));
                rows.add(Arrays.asList(editBtn, cancelBtn));

                markup.setKeyboard(rows);
                photo.setReplyMarkup(markup);
                execute(photo);
            } else {
                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(chatId));
                msg.setText("📋 Reklama ko'rinishi:\n\n" + adText);
                msg.setParseMode("HTML");

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                InlineKeyboardButton sendBtn = new InlineKeyboardButton();
                sendBtn.setText("✅ Reklamani Yuborish");
                sendBtn.setCallbackData("ad_send");

                InlineKeyboardButton editBtn = new InlineKeyboardButton();
                editBtn.setText("✏️ Tahrirlash");
                editBtn.setCallbackData("ad_edit");

                InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
                cancelBtn.setText("❌ Bekor qilish");
                cancelBtn.setCallbackData("ad_cancel");

                rows.add(Collections.singletonList(sendBtn));
                rows.add(Arrays.asList(editBtn, cancelBtn));

                markup.setKeyboard(rows);
                msg.setReplyMarkup(markup);
                execute(msg);
            }
        }

        // Reklamani yuborish
        private void broadcastAd(long chatId) throws TelegramApiException {
            String adText = adTextMap.get(chatId);
            String photoId = adPhotoMap.get(chatId);

            int successCount = 0;
            int failCount = 0;

            Set<Long> usersToSend = new HashSet<>(userUsernameMap.keySet());
            usersToSend.removeAll(ADMIN_IDS);

            sendText(chatId, "🔄 Reklama yuborilmoqda...\n" +
                    "Jami foydalanuvchilar: " + usersToSend.size() + " ta\n\n" +
                    "⏳ Jarayon davom etmoqda...");

            for (Long userId : usersToSend) {
                try {
                    if (photoId != null && !photoId.isEmpty()) {
                        SendPhoto photo = new SendPhoto();
                        photo.setChatId(String.valueOf(userId));
                        photo.setPhoto(new InputFile(photoId));
                        photo.setCaption(adText);
                        photo.setParseMode("HTML");
                        execute(photo);
                    } else {
                        SendMessage msg = new SendMessage();
                        msg.setChatId(String.valueOf(userId));
                        msg.setText(adText);
                        msg.setParseMode("HTML");
                        execute(msg);
                    }
                    successCount++;
                    Thread.sleep(100);
                } catch (Exception e) {
                    failCount++;
                }
            }

            String result = "✅ REKLAMA YUBORISH YAKUNLANDI\n\n" +
                    "📊 Natijalar:\n" +
                    "• Jami foydalanuvchilar: " + usersToSend.size() + " ta\n" +
                    "• Muvaffaqiyatli: " + successCount + " ta\n" +
                    "• Xatolik: " + failCount + " ta\n" +
                    "• Adminlar: " + ADMIN_IDS.size() + " ta (yuborilmadi)\n\n" +
                    "🕐 Yuborish vaqti: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            sendText(chatId, result);

            adTextMap.remove(chatId);
            adPhotoMap.remove(chatId);
            stateMap.put(chatId, "");
        }

        // Kanalga post yuborish
        private void postToChannel(long userId, int adNumber) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(userId);
            if (userPhotos == null || userPhotos.isEmpty()) {
                System.out.println("❌ Rasmlar topilmadi!");
                return;
            }

            saveUserAd(userId, adNumber);

            String adType = adTypeMap.getOrDefault(userId, "");
            saveStatistics(userId, adType, adNumber);

            List<String> photos = new ArrayList<>();
            String videoFileId = null;

            for (String media : userPhotos) {
                if (media.startsWith("video:")) {
                    videoFileId = media.substring(6);
                } else {
                    photos.add(media);
                }
            }

            Integer channelMessageId = null;

            if (videoFileId != null && !photos.isEmpty()) {
                channelMessageId = sendVideoWithPhotosToChannel(userId, videoFileId, photos, adNumber);
            }
            else if (videoFileId != null) {
                channelMessageId = sendVideoToChannel(userId, videoFileId, adNumber);
            }
            else if (!photos.isEmpty()) {
                channelMessageId = sendPhotosToChannel(userId, photos, adNumber);
            }

            if (channelMessageId != null) {
                List<UserAd> userAds = userAdsMap.get(userId);
                if (userAds != null && !userAds.isEmpty()) {
                    UserAd lastAd = userAds.get(userAds.size() - 1);
                    lastAd.channelMessageId = channelMessageId;
                    System.out.println("✅ Kanaldagi post ID saqlandi: " + channelMessageId + " reklama uchun: " + lastAd.adId);
                }
            }

            userHasPendingAdMap.put(userId, false);
        }

        // Video va rasmlarni kanalga yuborish
        private Integer sendVideoWithPhotosToChannel(long userId, String videoFileId, List<String> photos, int adNumber) throws TelegramApiException {
            String caption = buildChannelCaption(userId, adTypeMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""), phoneMap.getOrDefault(userId, ""), adNumber);

            try {
                SendVideo videoMsg = new SendVideo();
                videoMsg.setChatId(CHANNEL_USERNAME);
                videoMsg.setVideo(new InputFile(videoFileId));
                videoMsg.setCaption(caption);
                videoMsg.setParseMode("Markdown");
                Message sentMessage = execute(videoMsg);

                Integer messageId = sentMessage.getMessageId();

                for (String photo : photos) {
                    SendPhoto photoMsg = new SendPhoto();
                    photoMsg.setChatId(CHANNEL_USERNAME);
                    photoMsg.setPhoto(new InputFile(photo));
                    execute(photoMsg);
                }

                return messageId;

            } catch (Exception e) {
                System.out.println("❌ Video+rasm yuborishda xatolik: " + e.getMessage());
                SendVideo videoMsg = new SendVideo();
                videoMsg.setChatId(CHANNEL_USERNAME);
                videoMsg.setVideo(new InputFile(videoFileId));
                videoMsg.setCaption(caption);
                videoMsg.setParseMode("Markdown");
                Message sentMessage = execute(videoMsg);
                return sentMessage.getMessageId();
            }
        }

        // Videoni kanalga yuborish
        private Integer sendVideoToChannel(long userId, String videoFileId, int adNumber) throws TelegramApiException {
            String caption = buildChannelCaption(userId, adTypeMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""), phoneMap.getOrDefault(userId, ""), adNumber);

            SendVideo videoMsg = new SendVideo();
            videoMsg.setChatId(CHANNEL_USERNAME);
            videoMsg.setVideo(new InputFile(videoFileId));
            videoMsg.setCaption(caption);
            videoMsg.setParseMode("Markdown");
            Message sentMessage = execute(videoMsg);
            return sentMessage.getMessageId();
        }

        // Rasmlarni kanalga yuborish
        private Integer sendPhotosToChannel(long userId, List<String> photos, int adNumber) throws TelegramApiException {
            String caption = buildChannelCaption(userId, adTypeMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""), phoneMap.getOrDefault(userId, ""), adNumber);

            if (photos.size() >= 2) {
                SendMediaGroup mediaGroup = new SendMediaGroup();
                mediaGroup.setChatId(CHANNEL_USERNAME);

                List<InputMedia> mediaList = new ArrayList<>();

                InputMediaPhoto media1 = new InputMediaPhoto();
                media1.setMedia(photos.get(0));
                media1.setCaption(caption);
                media1.setParseMode("Markdown");
                mediaList.add(media1);

                for (int i = 1; i < photos.size(); i++) {
                    InputMediaPhoto media = new InputMediaPhoto();
                    media.setMedia(photos.get(i));
                    mediaList.add(media);
                }

                mediaGroup.setMedias(mediaList);
                List<Message> messages = execute(mediaGroup);
                if (messages != null && !messages.isEmpty()) {
                    return messages.get(0).getMessageId();
                }
                return null;
            } else if (photos.size() == 1) {
                SendPhoto post = new SendPhoto();
                post.setChatId(CHANNEL_USERNAME);
                post.setPhoto(new InputFile(photos.get(0)));
                post.setCaption(caption);
                post.setParseMode("Markdown");
                Message sentMessage = execute(post);
                return sentMessage.getMessageId();
            }
            return null;
        }

        // Kanal caption qurish
        private String buildChannelCaption(long userId, String adType, String manzil, String phone, int adNumber) {
            StringBuilder caption = new StringBuilder();

            String breed = breedMap.getOrDefault(userId, "");
            String age = ageMap.getOrDefault(userId, "");
            String gender = genderMap.getOrDefault(userId, "");
            String health = healthMap.getOrDefault(userId, "");
            String sterilization = sterilizationMap.getOrDefault(userId, "");
            String valyuta = valyutaMap.getOrDefault(userId, "so'm");
            String narxBelgisi = "so'm".equals(valyuta) ? " so'm" : " $";

            int userNumber = userNumberMap.getOrDefault(userId, 0);
            String username = userUsernameMap.getOrDefault(userId, "");

            if ("hadiya".equals(adType)) {
                caption.append("#HADIYA 🎁\n\n");
                caption.append("📝 Mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing\n\n");
                caption.append("🏠 Manzil: ").append(manzil).append("\n");
                caption.append("📞 Nomer: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("[YouTube](https://youtu.be/vdwgSB7_amw)");
                caption.append(" \uD83C\uDF10[Instagram](https://www.instagram.com/p/C-cZkgstVGK/)  ");
                caption.append(" ✉\uFE0F[Telegram](https://t.me/uzbek_cats)");

            } else if ("vyazka".equals(adType)) {
                caption.append("#VYAZKA 💝\n\n");
                caption.append("📝").append(breed).append(" ").append(age).append(" ").append(gender.toLowerCase())
                        .append(" ").append(sterilization).append(" ").append(health.toLowerCase()).append("\n\n");
                caption.append("🏠 Manzil: ").append(manzil).append("\n");
                caption.append("📞 Nomer: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("[YouTube](https://youtu.be/vdwgSB7_amw)");
                caption.append(" \uD83C\uDF10[Instagram](https://www.instagram.com/p/C-cZkgstVGK/)");
                caption.append(" ✉\uFE0F[Telegram](https://t.me/uzbek_cats)");

            } else {
                caption.append("#SOTILADI 💰\n\n");
                caption.append("📝").append(breed).append(" ").append(age).append(" ").append(gender.toLowerCase())
                        .append(" ").append(sterilization).append(" ").append(health.toLowerCase()).append("\n\n");
                caption.append("📍 Manzil: ").append(manzil).append("\n");
                caption.append("💵 Narxi: ").append(priceMap.getOrDefault(userId, "")).append(narxBelgisi).append("\n");
                caption.append("📞 Tel: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("[YouTube](https://youtu.be/vdwgSB7_amw)");
                caption.append(" \uD83C\uDF10[Instagram](https://www.instagram.com/p/C-cZkgstVGK/)");
                caption.append(" ✉\uFE0F[Telegram](https://t.me/uzbek_cats)");
            }

            return caption.toString();
        }

        // File URL olish
        private String getFileUrl(String fileId) throws TelegramApiException {
            try {
                org.telegram.telegrambots.meta.api.objects.File file =
                        execute(new org.telegram.telegrambots.meta.api.methods.GetFile(fileId));

                if (file != null && file.getFilePath() != null) {
                    return "https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + file.getFilePath();
                }
                return fileId;
            } catch (Exception e) {
                System.err.println("File URL olishda xatolik: " + e.getMessage());
                return fileId;
            }
        }

        // Statistikani saqlash
        private void saveStatistics(long userId, String adType, int adNumber) {
            String breed = breedMap.getOrDefault(userId, "");
            String phone = phoneMap.getOrDefault(userId, "");
            String username = userUsernameMap.getOrDefault(userId, "user_" + userId);

            AdRecord record = new AdRecord(userId, username, adType, breed, phone, adNumber);
            statisticsMap.get(adType).add(record);
        }

        // Adminlarga yordam so'rovini bildirish
        private void notifyAdminForYordam(long chatId, String type) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);

            String adminText = "\uD83D\uDE91 YANGI YORDAM SO'ROVI\n\n" +
                    "Turi: " + type + "\n" +
                    "User ID: " + chatId + "\n" +
                    "Manzil: " + manzilMap.getOrDefault(chatId, "—") + "\n" +
                    "Telefon: " + phoneMap.getOrDefault(chatId, "—") + "\n\n" +
                    "Tasdiqlaysizmi?";

            if (userPhotos != null && !userPhotos.isEmpty()) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(ADMIN_ID));
                photo.setPhoto(new InputFile(userPhotos.get(0)));
                photo.setCaption(adminText);

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton approve = new InlineKeyboardButton();
                approve.setText("✅ Tasdiqlash");
                approve.setCallbackData("yordam_approve_" + chatId);

                InlineKeyboardButton decline = new InlineKeyboardButton();
                decline.setText("❌ Rad etish");
                decline.setCallbackData("yordam_decline_" + chatId);

                markup.setKeyboard(Arrays.asList(
                        Collections.singletonList(approve),
                        Collections.singletonList(decline)
                ));
                photo.setReplyMarkup(markup);

                execute(photo);
            } else {
                SendMessage msg = new SendMessage();
                msg.setChatId(String.valueOf(ADMIN_ID));
                msg.setText(adminText);

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                InlineKeyboardButton approve = new InlineKeyboardButton();
                approve.setText("✅ Tasdiqlash");
                approve.setCallbackData("yordam_approve_" + chatId);

                InlineKeyboardButton decline = new InlineKeyboardButton();
                decline.setText("❌ Rad etish");
                decline.setCallbackData("yordam_decline_" + chatId);

                markup.setKeyboard(Arrays.asList(
                        Collections.singletonList(approve),
                        Collections.singletonList(decline)
                ));
                msg.setReplyMarkup(markup);

                execute(msg);
            }
        }

        // Yordam postini kanalga yuborish
        private void postYordamToChannel(long userId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(userId);
            String state = stateMap.get(userId);

            String caption = "#YORDAM\n\n";

            if (state.equals("yordam_onasiz")) {
                caption += "🐱 Mushukgimni onasi yuq yozrdam kerak\n\n";
                caption += "mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing\n\n";
                caption += "📍 Manzil: " + manzilMap.getOrDefault(userId, "—") + "\n";
                caption += "📞 Telefon: " + phoneMap.getOrDefault(userId, "—") + "\n\n";
            } else if (state.equals("yordam_kasal")) {
                caption += "🏥 Mushukgim kanal yordam kerak\n\n";
                caption += "Kasal mushukka yordam kerak. Iltimos, mushukni davolash uchun yordam bering!\n\n";
                caption += "📍 Manzil: " + manzilMap.getOrDefault(userId, "—") + "\n";
                caption += "📞 Telefon: " + phoneMap.getOrDefault(userId, "—") + "\n\n";
            } else if (state.equals("yordam_kasal_hadiya")) {
                caption += "🎁 Mushukgim kasal tekinga beraman\n\n";
                caption += "Mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing";
                caption += "📍 Manzil: " + manzilMap.getOrDefault(userId, "—") + "\n";
                caption += "📞 Telefon: " + phoneMap.getOrDefault(userId, "—") + "\n\n";
            }

            int userNumber = userNumberMap.getOrDefault(userId, 0);
            String username = userUsernameMap.getOrDefault(userId, "");
            caption += "👤 Foydalanuvchi: #" + userNumber + " " + username + "\n\n";
            caption += "[\uD83D\uDC51 Admin](https://t.me/zayd_catlover) | \n\n";
            caption += "[\uD83C\uDFAC YouTube](https://youtu.be/vdwgSB7_amw) | ";
            caption += "[\uD83D\uDC8E Instagram](https://www.instagram.com/p/C-cZkgstVGK/) | ";
            caption += "[\uD83D\uDCAC Telegram](https://t.me/uzbek_cats)";

            if (userPhotos != null && !userPhotos.isEmpty()) {
                SendPhoto post = new SendPhoto();
                post.setChatId(CHANNEL_USERNAME);
                post.setPhoto(new InputFile(userPhotos.get(0)));
                post.setCaption(caption);
                post.setParseMode("Markdown");
                execute(post);
            } else {
                SendMessage post = new SendMessage();
                post.setChatId(CHANNEL_USERNAME);
                post.setText(caption);
                post.setParseMode("Markdown");
                execute(post);
            }
        }

        // Adminlarga bildirish
        private void notifyAdmin(long chatId) throws TelegramApiException {
            long adId = adIdCounter.incrementAndGet();

            String caption = "🆕 Yangi e'lon! ID: " + adId + "\n\n" +
                    "Tur: " + adTypeMap.getOrDefault(chatId, "") + "\n" +
                    buildPreviewCaption(chatId);

            SendMessage info = new SendMessage();
            info.setChatId(String.valueOf(ADMIN_ID));
            info.setText(caption);
            Message sentMessage = execute(info);
            adminMessageIds.put(sentMessage.getMessageId(), chatId);

            List<String> userPhotos = photosMap.get(chatId);
            if (userPhotos != null && !userPhotos.isEmpty()) {
                String mediaType = mediaTypeMap.getOrDefault(chatId, "photo");

                if ("video".equals(mediaType) && userPhotos.get(0).startsWith("video:")) {
                    String videoFileId = userPhotos.get(0).substring(6);
                    SendVideo video = new SendVideo();
                    video.setChatId(String.valueOf(ADMIN_ID));
                    video.setVideo(new InputFile(videoFileId));
                    video.setCaption("E'lon videosi - ID: " + adId);

                    addAdminButtons(video, chatId, adId);

                    Message videoMessage = execute(video);
                    adminMessageIds.put(videoMessage.getMessageId(), chatId);
                } else {
                    SendPhoto photo = new SendPhoto();
                    photo.setChatId(String.valueOf(ADMIN_ID));
                    photo.setPhoto(new InputFile(userPhotos.get(0)));
                    photo.setCaption("E'lon rasmi - ID: " + adId);

                    addAdminButtons(photo, chatId, adId);

                    Message photoMessage = execute(photo);
                    adminMessageIds.put(photoMessage.getMessageId(), chatId);
                }
            }

            if (checkMap.containsKey(chatId)) {
                SendPhoto check = new SendPhoto();
                check.setChatId(String.valueOf(ADMIN_ID));
                check.setPhoto(new InputFile(checkMap.get(chatId)));
                check.setCaption("💳 To'lov cheki - ID: " + adId);

                addAdminButtons(check, chatId, adId);

                Message checkMessage = execute(check);
                adminMessageIds.put(checkMessage.getMessageId(), chatId);
            }
        }

        // Admin tugmalarini qo'shish
        private void addAdminButtons(Object message, long chatId, long adId) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

            InlineKeyboardButton approve = new InlineKeyboardButton();
            approve.setText("✅ Tasdiqlash");
            approve.setCallbackData("approve_" + chatId);

            InlineKeyboardButton edit = new InlineKeyboardButton();
            edit.setText("✏️ O'zgartirish");
            edit.setCallbackData("edit_" + chatId);

            InlineKeyboardButton decline = new InlineKeyboardButton();
            decline.setText("❌ Rad etish");
            decline.setCallbackData("decline_" + chatId);

            markup.setKeyboard(Arrays.asList(
                    Arrays.asList(approve, edit),
                    Collections.singletonList(decline)
            ));

            if (message instanceof SendPhoto) {
                ((SendPhoto) message).setReplyMarkup(markup);
            } else if (message instanceof SendVideo) {
                ((SendVideo) message).setReplyMarkup(markup);
            }
        }

        // Admin xabarlarini o'chirish
        private void deleteAdminMessages(long userId) {
            try {
                System.out.println("🗑️ Foydalanuvchi ma'lumotlari o'chirilmoqda: " + userId);

                photosMap.remove(userId);
                breedMap.remove(userId);
                ageMap.remove(userId);
                healthMap.remove(userId);
                genderMap.remove(userId);
                priceMap.remove(userId);
                checkMap.remove(userId);
                adTypeMap.remove(userId);
                mushukSoniMap.remove(userId);
                sterilizationMap.remove(userId);
                platformaMap.remove(userId);
                valyutaMap.remove(userId);
                stateMap.remove(userId);
                declineReasonsMap.remove(userId);
                mediaTypeMap.remove(userId);

                deleteAdminPanelMessages(userId);

                System.out.println("✅ Barcha ma'lumotlar va admin xabarlari o'chirildi: " + userId);

            } catch (Exception e) {
                System.out.println("❌ Xabarlarni o'chirishda xatolik: " + e.getMessage());
            }
        }

        // Admin panel xabarlarini o'chirish
        private void deleteAdminPanelMessages(long userId) {
            try {
                List<Integer> messagesToDelete = new ArrayList<>();

                for (Map.Entry<Integer, Long> entry : adminMessageIds.entrySet()) {
                    if (entry.getValue() == userId) {
                        messagesToDelete.add(entry.getKey());
                    }
                }

                for (Integer messageId : messagesToDelete) {
                    try {
                        DeleteMessage deleteMsg = new DeleteMessage();
                        deleteMsg.setChatId(String.valueOf(ADMIN_ID));
                        deleteMsg.setMessageId(messageId);
                        execute(deleteMsg);
                        adminMessageIds.remove(messageId);
                    } catch (Exception e) {
                        System.out.println("Admin xabarini o'chirishda xatolik: " + messageId);
                    }
                }

                System.out.println("✅ " + messagesToDelete.size() + " ta admin xabari o'chirildi");

            } catch (Exception e) {
                System.out.println("❌ Admin xabarlarini o'chirishda xatolik: " + e.getMessage());
            }
        }

        // Vyazka uchun viloyat tanlanganidan keyin
        private void sendViloyatSelectionForVyazka(long chatId, String viloyat) throws TelegramApiException {
            manzilMap.put(chatId, viloyat);

            String diniyMatn = "Ассалому алайкум! Мени мушугим зотли. Бирон бир зотли мушукка чатиштиришга берсам ёки қўштирганимга пул олсам бўладими?\n\n" +
                    "«Зикр аҳлидан сўранг» ҳайъати:\n" +
                    "– Ва алайкум ассалом! Ҳалол эмас. \n\n" +
                    "عَنِ ابْنِ عُمَرَ رَضِي اللهُ عَنْهُما قَالَ: نَهَى النَّبِيُّ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ عَنْ عَسْبِ الْفَحْلِ. رَوَاهُ الْخَمْسَةُ إِلَّا مُسْلِمًا\n\n" +
                    "Ибн Умар розияллоҳу анҳумодан ривоят қилинади:\n" +
                    "«Набий соллаллоҳу алайҳи васаллам эркак ҳайвоннинг қочириши (учун ҳақ олиш)дан наҳий қилдилар».\n\n" +
                    "Шарҳ: Жоҳилият пайтида урғочи ҳайвонларни қочириш учун қўшиб қўйиладиган эркак ҳайвонлар учун ҳам ҳақ олишар эди. Бу иш номаълум нарсага ҳақ олиш бўлганлиги учун Исломда бекор қилинди. Ундай ҳайвонларни урғочи ҳайвон қочириб олингунча вақтинчага, бепул бериб туриш тавсия қилинди. Демак, мазкур нотўғри йўл билан мол касб қилиш ҳам жоиз эмас. (\"Ҳадис ва Ҳаёт\" китобидан). Валлоҳу аълам!";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(diniyMatn);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton confirmBtn = new InlineKeyboardButton();
            confirmBtn.setText("✅ Ko'rib chiqdim, davom etish");
            confirmBtn.setCallbackData("vyazka_diniy_confirm");

            markup.setKeyboard(Collections.singletonList(Collections.singletonList(confirmBtn)));
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // Valyuta tanlash
        private void sendValyutaSelection(long chatId) throws TelegramApiException {
            String adType = adTypeMap.getOrDefault(chatId, "");

            if ("vyazka".equals(adType)) {
                String viloyat = manzilMap.getOrDefault(chatId, "");
                sendViloyatSelectionForVyazka(chatId, viloyat);
                return;
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("💰 Valyutani tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton somBtn = new InlineKeyboardButton();
            somBtn.setText("🇺🇿 So'mda");
            somBtn.setCallbackData("valyuta_som");
            rows.add(Collections.singletonList(somBtn));

            InlineKeyboardButton dollarBtn = new InlineKeyboardButton();
            dollarBtn.setText("💵 Dollarda");
            dollarBtn.setCallbackData("valyuta_dollar");
            rows.add(Collections.singletonList(dollarBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // To'lov ko'rsatmalari
        private void sendPaymentInstructions(long chatId) throws TelegramApiException {
            String adType = adTypeMap.getOrDefault(chatId, "");
            int narx = 0;
            String mushukText = "";

            if ("vyazka".equals(adType)) {
                narx = 100000;
                mushukText = "vyazka (juft topish) uchun";
            } else {
                int mushukSoni = mushukSoniMap.getOrDefault(chatId, 1);
                switch (mushukSoni) {
                    case 1: narx = 35000; mushukText = "1 ta mushuk"; break;
                    case 2: narx = 70000; mushukText = "2 ta mushuk"; break;
                    case 3: narx = 105000; mushukText = "3 ta mushuk"; break;
                    case 4: narx = 120000; mushukText = "4 ta mushuk"; break;
                    case 5: narx = 150000; mushukText = "5 ta mushuk"; break;
                    case 6: narx = 150000; mushukText = "+5 ta mushuk"; break;
                    default: narx = 35000; mushukText = "1 ta mushuk"; break;
                }
            }

            String message = "💳 To'lov ma'lumotlari:\n\n" +
                    "Siz " + mushukText + " to'lov qilishingiz kerak:\n\n" +
                    "💵 To'lov miqdori: " + String.format("%,d", narx) + " so'm\n" +
                    "💳 Karta raqam: 5614681626280956\n" +
                    "👤 Karta egasi: Xalilov.A\n\n" +
                    "To'lov qilib, chekining rasmini yuboring.";

            sendText(chatId, message);
            stateMap.put(chatId, "wait_check");
        }

        // Guruh xabarlarini tekshirish
        private void handleGroupMessage(Message message) throws TelegramApiException {
            checkAndDeleteAd(message);
        }

        // Reklamani tekshirish va o'chirish
        private void checkAndDeleteAd(Message message) throws TelegramApiException {
            boolean shouldDelete = false;
            String reason = "";

            if (message.hasText()) {
                String text = message.getText().toLowerCase();

                for (String bannedWord : bannedWords) {
                    if (text.contains(bannedWord)) {
                        shouldDelete = true;
                        reason = "Taqiqlangan so'z: " + bannedWord;
                        break;
                    }
                }

                if (!shouldDelete && containsUrl(text)) {
                    shouldDelete = true;
                    reason = "URL yoki havola topildi";
                }
            }

            if (!shouldDelete && (message.hasPhoto() || message.hasVideo())) {
                String caption = message.getCaption();
                if (caption != null) {
                    String text = caption.toLowerCase();

                    for (String bannedWord : bannedWords) {
                        if (text.contains(bannedWord)) {
                            shouldDelete = true;
                            reason = "Rasm/video captionida taqiqlangan so'z: " + bannedWord;
                            break;
                        }
                    }

                    if (!shouldDelete && containsUrl(text)) {
                        shouldDelete = true;
                        reason = "Rasm/video captionida URL";
                    }
                }
            }

            if (shouldDelete) {
                deleteMessage(message.getChatId(), message.getMessageId());
                sendWarningMessage(message.getChatId(), message.getFrom().getId());
            }
        }

        // URL borligini tekshirish
        private boolean containsUrl(String text) {
            if (text == null) return false;
            String lowerText = text.toLowerCase();

            String[] urlPatterns = {
                    "http://", "https://", "www.", ".com", ".uz", ".net", ".org",
                    "t.me/", "telegram.me/", "instagram.com/", "youtube.com/",
                    "youtu.be/", "facebook.com/", "fb.com/", "twitter.com/",
                    "chat.whatsapp.com/", "wa.me/"
            };

            for (String pattern : urlPatterns) {
                if (lowerText.contains(pattern)) {
                    return true;
                }
            }

            if (lowerText.matches(".*@[a-zA-Z0-9_]{5,}.*")) {
                return true;
            }

            return false;
        }

        // Xabarni o'chirish
        private void deleteMessage(Long chatId, Integer messageId) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(messageId);
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Xabarni o'chirishda xatolik: " + e.getMessage());
            }
        }

        // Ogohlantirish xabarini yuborish
        private void sendWarningMessage(Long chatId, Long userId) {
            try {
                SendMessage warning = new SendMessage();
                warning.setChatId(chatId.toString());
                warning.setText(WARNING_MESSAGE + "\n\n👤 Foydalanuvchi ID: " + userId);
                execute(warning);
            } catch (TelegramApiException e) {
                System.out.println("Ogohlantirish yuborishda xatolik: " + e.getMessage());
            }
        }

        // Guruh yoki kanal ekanligini tekshirish
        private boolean isGroupOrChannel(Long chatId) {
            try {
                Chat chat = getChat(chatId.toString());
                return chat.isGroupChat() || chat.isSuperGroupChat() || chat.isChannelChat();
            } catch (TelegramApiException e) {
                return false;
            }
        }

        // Chat ma'lumotlarini olish
        private Chat getChat(String chatId) throws TelegramApiException {
            GetChat getChat = new GetChat(chatId);
            return execute(getChat);
        }

        // Matn xabarini yuborish
        private void sendText(long chatId, String text) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(text);
            execute(msg);
        }

        // Kanal postini qayta ishlash
        private void handleChannelPost(Message message) throws TelegramApiException {
            if (message.getChat().getUserName() != null &&
                    message.getChat().getUserName().equalsIgnoreCase(CHANNEL_USERNAME.replace("@", ""))) {
                return;
            }
            checkAndDeleteAd(message);
        }

        // Qo'shimcha eski metodlar uchun
        private void sendKonkursReferralSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🔢 *Qaysi o'rindagi foydalanuvchini ko'rmoqchisiz?* (1-10):");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // 5 ta qator, har qatorda 2 ta tugma
            for (int i = 1; i <= 10; i += 2) {
                List<InlineKeyboardButton> row = new ArrayList<>();

                InlineKeyboardButton btn1 = new InlineKeyboardButton();
                btn1.setText(String.valueOf(i));
                btn1.setCallbackData("referral_rank_" + i);
                row.add(btn1);

                if (i+1 <= 10) {
                    InlineKeyboardButton btn2 = new InlineKeyboardButton();
                    btn2.setText(String.valueOf(i+1));
                    btn2.setCallbackData("referral_rank_" + (i+1));
                    row.add(btn2);
                }

                rows.add(row);
            }

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("tech_konkurs_takliflar");

            rows.add(Collections.singletonList(backBtn));
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        private void showReferralDetails(long adminId, int rank) throws TelegramApiException {
            // Top 10 ni olish
            List<Map.Entry<Long, Integer>> topUsers = userScores.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (rank > topUsers.size()) {
                sendText(adminId, "❌ " + rank + "-o'rin mavjud emas!");
                return;
            }

            Map.Entry<Long, Integer> userEntry = topUsers.get(rank - 1);
            Long userId = userEntry.getKey();

            // Ushbu foydalanuvchi qo'shgan odamlarni topish
            List<Long> referrals = new ArrayList<>();
            for (Map.Entry<Long, Long> refEntry : referralMap.entrySet()) {
                if (refEntry.getValue().equals(userId)) {
                    referrals.add(refEntry.getKey());
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("👤 #").append(userNumberMap.getOrDefault(userId, 0))
                    .append(" (").append(rank).append("-o'rin)\n")
                    .append("📊 Ball: ").append(userEntry.getValue()).append("\n")
                    .append("👥 Qo'shganlar soni: ").append(referrals.size()).append("\n\n");

            if (!referrals.isEmpty()) {
                result.append("🔢 Qo'shgan foydalanuvchilar ID lari:\n");
                for (Long refId : referrals) {
                    result.append("• #").append(userNumberMap.getOrDefault(refId, 0)).append("\n");
                }
            } else {
                result.append("⚠️ Hali hech kim qo'shmagan");
            }

            sendText(adminId, result.toString());
        }

        private void sendAdminRatingManagement(long chatId) throws TelegramApiException {
            // Top 10 foydalanuvchini olish
            List<Map.Entry<Long, Integer>> topUsers = userScores.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            StringBuilder ratingText = new StringBuilder();
            ratingText.append("🏆 *Top 10 foydalanuvchi (ID bo'yicha):*\n\n");

            for (int i = 0; i < topUsers.size(); i++) {
                Map.Entry<Long, Integer> entry = topUsers.get(i);
                Long userId = entry.getKey();
                Integer score = entry.getValue();

                int userNumber = userNumberMap.getOrDefault(userId, 0);
                ratingText.append(i+1).append(". #").append(userNumber)
                        .append(" - ").append(score).append(" ball\n");
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(ratingText.toString());
            msg.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Har bir foydalanuvchi uchun tugma
            for (int i = 0; i < topUsers.size(); i++) {
                Map.Entry<Long, Integer> entry = topUsers.get(i);
                Long userId = entry.getKey();

                InlineKeyboardButton userBtn = new InlineKeyboardButton();
                int userNumber = userNumberMap.getOrDefault(userId, 0);
                userBtn.setText("#" + userNumber + " ballarni 0 qilish");
                userBtn.setCallbackData("rating_reset_" + userId);
                rows.add(Collections.singletonList(userBtn));
            }

            // Orqaga
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("admin_panel");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }
    }
}// Mushukcha yaxshi insonlarga tekinga sovg'a qilinadi.
