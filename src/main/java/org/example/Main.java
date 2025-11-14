package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
        private static final String BOT_TOKEN = "8577521489:AAGbp2MvcMXZlnK-KbDdmPm8WArYlJ4PxWk";
        private final long ADMIN_ID = 673018191l;

        private final String CHANNEL_USERNAME = "@uzbek_cats";

        // State va ma'lumotlar
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
        private final AtomicLong adIdCounter = new AtomicLong(1000);

        // Admin ma'lumotlari
        private final Map<Long, String> declineReasonsMap = new ConcurrentHashMap<>();
        private final Map<Integer, Long> adminMessageIds = new ConcurrentHashMap<>();
        private final Map<Long, Long> adminEditUserIdMap = new ConcurrentHashMap<>();

        // Statistika ma'lumotlari
        private final Map<String, List<AdRecord>> statisticsMap = new ConcurrentHashMap<>();
        private final Map<Long, String> userUsernameMap = new ConcurrentHashMap<>();

        // Konkurs ma'lumotlari - ADMIN O'ZGARTIRISHI UCHUN
        private String currentKonkursImageUrl = "https://i.postimg.cc/YvGp1gHt/image.jpg";
        private String currentKonkursText = "🎁 Scottish fold black\n\nSiz toplagan ovoz ochib ketmaydi toki 🏆 g'olib bo'lgungizgacha 💯";

        // Reklama filtrlash
        private final Set<String> bannedWords = Set.of(
                "mushuk sotiladi",
                "mushuk bor",
                "reklama",
                "sotaman",
                "sotiladi"
        );
        private final String WARNING_MESSAGE = "❌ Iltimos, reklama tarqatmang!";

        // Viloyatlar ro'yxati
        private final List<String> viloyatlar = Arrays.asList(
                "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Xorazm",
                "Namangan", "Navoiy", "Qashqadaryo", "Samarqand",
                "Sirdaryo", "Surxondaryo", "Toshkent", "Toshkent shahar"
        );

        // Yoshlar
        private final List<String> ages = Arrays.asList(
                "1 oylik", "2 oylik", "3 oylik", "4 oylik", "5 oylik",
                "6 oylik", "7 oylik", "8 oylik", "9 oylik",
                "10 oylik", "11 oylik", "+1 yosh"
        );

        // Zotlar sahifalari
        private final Map<Integer, List<String>> breedPages = new HashMap<>();
        private final List<String> allBreeds = new ArrayList<>();

        // Logo URL
        private final String LOGO_URL = "https://i.postimg.cc/PCGRfS7g/image.png";

        // Konkurs ishtirokchilari
        private final List<KonkursParticipant> konkursParticipants = Arrays.asList(
                new KonkursParticipant("Muhammadiy", "@Muhammadi_1808", 202, "https://t.me/Muhammadi_1808"),
                new KonkursParticipant("Abdulloh Sheraliyev", "@Abdulloo789", 193, "https://t.me/Abdulloo789"),
                new KonkursParticipant("Muslimbek", "@Muslimaxon_93", 155, "https://t.me/Muslimaxon_93"),
                new KonkursParticipant("ishtopchiuz | admin", "@premium_oberam1z", 136, "https://t.me/premium_oberam1z"),
                new KonkursParticipant("DarkAce", "@Dubai_070", 125, "https://t.me/Dubai_070"),
                new KonkursParticipant(".....", "", 107, ""),
                new KonkursParticipant("𝓓𝓻_𝓫𝓸𝓫𝓸𝔁𝓸𝓷𝓸𝓿𝓷𝓪🦋", "@dr_sadullayevna", 100, "https://t.me/dr_sadullayevna"),
                new KonkursParticipant("🫀", "@top_banana_9", 91, "https://t.me/top_banana_9"),
                new KonkursParticipant("Ozodbek", "@Yuldowev_Ozodbek", 88, "https://t.me/Yuldowev_Ozodbek"),
                new KonkursParticipant("Марьям", "@OlloxBor", 82, "https://t.me/OlloxBor"),
                new KonkursParticipant("𝐔𝐌𝐈𝐃⦁🇲🇪⦁𝐒𝐎𝐕𝐄𝐓𝐒𝐊𝐈", "@ham1dullayevic_1", 81, "https://t.me/ham1dullayevic_1"),
                new KonkursParticipant("O'chirilgan hisob", "@yunusovarobiya_2283", 78, "https://t.me/yunusovarobiya_2283"),
                new KonkursParticipant("flirtyhh*", "@koxinur7713", 73, "https://t.me/koxinur7713"),
                new KonkursParticipant("...", "", 72, ""),
                new KonkursParticipant("N_A_A", "@Nilufar_Azimovaa", 66, "https://t.me/Nilufar_Azimovaa"),
                new KonkursParticipant("Александр", "", 53, ""),
                new KonkursParticipant("😽", "@mushuk_savdoqarshi", 51, "https://t.me/mushuk_savdoqarshi"),
                new KonkursParticipant("معمورجان", "", 51, ""),
                new KonkursParticipant("Mr. Akhror", "@Murodilla_iskandarov", 44, "https://t.me/Murodilla_iskandarov"),
                new KonkursParticipant("𝕀ℝ𝕆𝔻𝔸", "@Iroda0620", 39, "https://t.me/Iroda0620")
        );

        // AdRecord klassi statistika uchun
        class AdRecord {
            long userId;
            String username;
            String adType;
            String breed;
            Date date;
            String phone;

            public AdRecord(long userId, String username, String adType, String breed, String phone) {
                this.userId = userId;
                this.username = username;
                this.adType = adType;
                this.breed = breed;
                this.phone = phone;
                this.date = new Date();
            }

            public String getFormattedDate() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                return sdf.format(date);
            }
        }

        // Konkurs ishtirokchisi klassi
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
            // Sahifa 1 zotlari
            List<String> page1 = Arrays.asList(
                    "Scottish fold", "Scottish straight", "British blue", "British chinchilla",
                    "Bengal cat", "Turkish angora", "Scottish chinchila", "Gibrid", "British Shorthair"
            );
            breedPages.put(1, page1);
            allBreeds.addAll(page1);

            // Sahifa 2 zotlari
            List<String> page2 = Arrays.asList(
                    "British Shorthair Britan qisqa junli", "British Longhair Britan uzun junli",
                    "Persian Cat Fors mushugi", "Maine Coon Meyn-kun", "Ragdoll Qo'g'irchoq mushuk",
                    "Bengal Cat Bengal mushugi", "Siberian Cat Sibir mushugi", "Russian blue Rus moviy mushugi"
            );
            breedPages.put(2, page2);
            allBreeds.addAll(page2);

            // Sahifa 3 zotlari
            List<String> page3 = Arrays.asList(
                    "Abyssinian Abissiniya", "Norwegian Forest Cat Norvegiya", "Turkish Angora Turkiya angora",
                    "Turkish Van Turkiya Van", "Burmilla burma + chinchilla aralash", "British Longhair Britan uzun junli",
                    "American Shorthair Amerika qisqa junli", "American Curl Amerika bukilgan quloqli", "Egyptian Mau Misr mau mushugi"
            );
            breedPages.put(3, page3);
            allBreeds.addAll(page3);

            // Sahifa 4 zotlari
            List<String> page4 = Arrays.asList(
                    "Tonkinese Tonkin siam va birma", "Balinese Balin uzun junli", "Exotic Shorthair Egzotik qisqa junli",
                    "Savannah Cat Savanna serval bilan aralash", "Munchkin kalta oyoqli mushuk", "Khao Manee Xao Mani",
                    "Uy mushuki", "Mushuk"
            );
            breedPages.put(4, page4);
            allBreeds.addAll(page4);

            // Statistika map'larini ishga tushirish
            statisticsMap.put("hadiya", new ArrayList<>());
            statisticsMap.put("sotish", new ArrayList<>());
            statisticsMap.put("vyazka", new ArrayList<>());
        }

        @Override
        public String getBotUsername() { return BOT_USERNAME; }
        @Override
        public String getBotToken() { return BOT_TOKEN; }

        @Override
        public void onUpdateReceived(Update update) {
            try {
                // Reklama filtrlash
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
                            userUsernameMap.put(userId, username);
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

        // ========== REKLAMA FILTR METODLARI ==========
        private void handleChannelPost(Message message) throws TelegramApiException {
            if (message.getChat().getUserName() != null &&
                    message.getChat().getUserName().equalsIgnoreCase(CHANNEL_USERNAME.replace("@", ""))) {
                return;
            }
            checkAndDeleteAd(message);
        }

        private void handleGroupMessage(Message message) throws TelegramApiException {
            checkAndDeleteAd(message);
        }

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
                System.out.println("Reklama o'chirildi: " + reason);
                deleteMessage(message.getChatId(), message.getMessageId());
                sendWarningMessage(message.getChatId(), message.getFrom().getId());
            }
        }

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

        private void sendWarningMessage(Long chatId, Long userId) {
            try {
                SendMessage warning = new SendMessage();
                warning.setChatId(chatId.toString());
                warning.setText(WARNING_MESSAGE + "\n\n👤 Foydalanuvchi ID: " + userId +
                        "\n📝 Sabab: Reklama tarqatish taqiqlanadi!");
                execute(warning);
            } catch (TelegramApiException e) {
                System.out.println("Ogohlantirish yuborishda xatolik: " + e.getMessage());
            }
        }

        private boolean isGroupOrChannel(Long chatId) {
            try {
                Chat chat = getChat(chatId.toString());
                return chat.isGroupChat() || chat.isSuperGroupChat() || chat.isChannelChat();
            } catch (TelegramApiException e) {
                return false;
            }
        }

        private Chat getChat(String chatId) throws TelegramApiException {
            GetChat getChat = new GetChat(chatId);
            return execute(getChat);
        }

        // ========== ASOSIY MESSAGE HANDLER ==========
        private void handleMessage(Message msg) throws Exception {
            long chatId = msg.getChatId();
            String state = stateMap.getOrDefault(chatId, "");

            if (msg.hasText() && msg.getText().equals("/start")) {
                String[] parts = msg.getText().split(" ");
                if (parts.length > 1 && parts[1].equals("reklama")) {
                    sendAdTypeSelection(chatId);
                } else {
                    sendMainMenu(chatId);
                }
                return;
            }

            // ========== ADMIN KONKURS O'ZGARTIRISH ==========
            if (chatId == ADMIN_ID) {
                // Admin konkurs rasmini yangilash
                if ("admin_await_konkurs_image".equals(state)) {
                    if (msg.hasPhoto()) {
                        List<PhotoSize> photos = msg.getPhoto();
                        String fileId = photos.get(photos.size()-1).getFileId();

                        // Yangi rasm URL'sini olish
                        String newImageUrl = getFileUrl(fileId);
                        currentKonkursImageUrl = newImageUrl;

                        // Endi matn so'raymiz
                        stateMap.put(chatId, "admin_await_konkurs_text");
                        sendText(chatId, "✅ Rasm qabul qilindi! Endi yangi konkurs matnini yuboring:");

                    } else {
                        sendText(chatId, "❌ Iltimos, faqat rasm yuboring!");
                    }
                    return;
                }

                // Admin konkurs matnini yangilash
                if ("admin_await_konkurs_text".equals(state)) {
                    if (msg.hasText()) {
                        currentKonkursText = msg.getText();
                        sendText(chatId, "✅ Konkurs rasmi va matni muvaffaqiyatli yangilandi!");
                        stateMap.put(chatId, "");

                        // Yangi rasm va matnni ko'rsatish
                        sendKonkursMukofot(chatId);
                    } else {
                        sendText(chatId, "❌ Iltimos, faqat matn yuboring!");
                    }
                    return;
                }
            }

            // Admin izoh qoldirish
            if (chatId == ADMIN_ID && state.startsWith("admin_decline_reason_")) {
                String userIdStr = state.substring("admin_decline_reason_".length());
                long userId = Long.parseLong(userIdStr);

                String reason = msg.getText();
                declineReasonsMap.put(userId, reason);

                sendText(userId, "❌ E'loningiz tasdiqlanmadi!\n\n📝 Sabab: " + reason);
                sendText(ADMIN_ID, "✅ Foydalanuvchiga rad etish sababi yuborildi.");

                stateMap.put(chatId, "");
                return;
            }

            // Admin ma'lumot o'zgartirish
            if (chatId == ADMIN_ID && state.startsWith("admin_edit_")) {
                String editType = state.substring("admin_edit_".length());
                Long userId = adminEditUserIdMap.get(chatId);

                if (userId != null) {
                    String newValue = msg.getText().trim();

                    switch (editType) {
                        case "manzil":
                            manzilMap.put(userId, newValue);
                            sendText(ADMIN_ID, "✅ Manzil o'zgartirildi: " + newValue);
                            break;
                        case "phone":
                            phoneMap.put(userId, newValue);
                            sendText(ADMIN_ID, "✅ Telefon raqami o'zgartirildi: " + newValue);
                            break;
                        case "price":
                            priceMap.put(userId, newValue);
                            sendText(ADMIN_ID, "✅ Narx o'zgartirildi: " + newValue);
                            break;
                    }

                    sendAdminEditMenu(ADMIN_ID, userId);
                    stateMap.put(chatId, "");
                }
                return;
            }

            // Custom zot kiritish
            if ("await_custom_breed".equals(state)) {
                breedMap.put(chatId, msg.getText().trim());
                sendAgeSelection(chatId);
                return;
            }

            // Narx kiritish
            if ("await_price".equals(state)) {
                priceMap.put(chatId, msg.getText().trim());
                sendPaymentInstructions(chatId);
                return;
            }

            // Chek qabul qilish
            if ("wait_check".equals(state)) {
                if (msg.hasPhoto()) {
                    List<PhotoSize> photos = msg.getPhoto();
                    String fileId = photos.get(photos.size()-1).getFileId();
                    checkMap.put(chatId, fileId);
                    sendText(chatId, "✅ Chek qabul qilindi. Admin tekshiradi.");
                    notifyAdmin(chatId);
                    stateMap.put(chatId, "waiting_admin");
                } else {
                    sendText(chatId, "❌ Iltimos, to'lov chekining rasmini yuboring.");
                }
                return;
            }

            if (msg.hasText()) {
                String text = msg.getText().trim();

                // Yordam uchun ma'lumot qabul qilish
                if ("yordam_await_info".equals(state)) {
                    String userInfo = text;
                    manzilMap.put(chatId, userInfo);
                    sendYordamPreview(chatId, userInfo);
                    return;
                }

                // Telefon raqam kiritish
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

                sendText(chatId, "Iltimos, tugmalardan foydalaning yoki /start ni bosing.");
                return;
            }

            // RASM qabul qilish
            if (msg.hasPhoto() && (
                    "await_photo".equals(state) ||
                            state.startsWith("yordam_") && state.endsWith("_photo")
            )) {
                List<PhotoSize> photos = msg.getPhoto();
                String fileId = photos.get(photos.size()-1).getFileId();

                if (!photosMap.containsKey(chatId)) {
                    photosMap.put(chatId, new ArrayList<>());
                }

                if (photosMap.get(chatId).size() < 3) {
                    photosMap.get(chatId).add(fileId);

                    int currentCount = photosMap.get(chatId).size();
                    if (currentCount >= 3) {
                        sendText(chatId, "✅ 3-rasm qabul qilindi. Maksimum 3 ta rasm yuborishingiz mumkin.");
                        if (!state.startsWith("yordam_")) {
                            sendContinueButton(chatId);
                        } else {
                            sendYordamViloyatSelection(chatId);
                        }
                    } else {
                        sendText(chatId, "✅ " + currentCount + "-rasm qabul qilindi. " +
                                (3 - currentCount) + " ta rasm yuborishingiz mumkin yoki 'Davom etish' tugmasini bosing.");
                        if (!state.startsWith("yordam_")) {
                            sendContinueButton(chatId);
                        }
                    }
                } else {
                    sendText(chatId, "❌ Maksimum 3 ta rasm yuborishingiz mumkin. 'Davom etish' tugmasini bosing.");
                    if (!state.startsWith("yordam_")) {
                        sendContinueButton(chatId);
                    }
                }
                return;
            }

            // VIDEO qabul qilish
            if (msg.hasVideo() && (
                    "await_photo".equals(state) ||
                            state.startsWith("yordam_") && state.endsWith("_photo")
            )) {
                Video video = msg.getVideo();

                if (video.getDuration() <= 10) {
                    String fileId = video.getFileId();

                    if (!photosMap.containsKey(chatId)) {
                        photosMap.put(chatId, new ArrayList<>());
                    }

                    photosMap.get(chatId).add("video:" + fileId);

                    sendText(chatId, "✅ Video qabul qilindi! (10 soniyagacha)\n\nEndi rasmlarni yuboring yoki 'Davom etish' tugmasini bosing.");
                    if (!state.startsWith("yordam_")) {
                        sendContinueButton(chatId);
                    }

                } else {
                    sendText(chatId, "❌ Video 10 soniyadan uzun! Iltimos, 10 soniyagacha bo'lgan video yuboring.");
                }
                return;
            }
        }

        // ========== CALLBACK HANDLER ==========
        private void handleCallback(CallbackQuery cb) throws Exception {
            long chatId = cb.getMessage().getChatId();
            String data = cb.getData();
            long fromId = cb.getFrom().getId();

            execute(new AnswerCallbackQuery(cb.getId()));

            System.out.println("Callback received: " + data + " from: " + chatId);

            // ========== ADMIN KONKURS O'ZGARTIRISH ==========
            if (data.equals("admin_konkurs_image")) {
                if (fromId == ADMIN_ID) {
                    handleAdminKonkursImage(chatId);
                }
                return;
            }

            // Admin o'zgartirish callbacks
            if (data.startsWith("admin_set_breed_")) {
                handleAdminSetBreed(chatId, data);
                return;
            }

            if (data.startsWith("admin_edit_field_")) {
                handleAdminEditField(chatId, data);
                return;
            }

            // Zot sahifalari callbacks
            if (data.startsWith("breed_page_")) {
                int page = Integer.parseInt(data.substring("breed_page_".length()));
                sendBreedSelectionWithCustom(chatId, page);
                return;
            }

            // Zot tanlash callbacks
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

            // Platforma tanlash (vyazka uchun)
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

                stateMap.put(chatId, "await_photo");
                photosMap.put(chatId, new ArrayList<>());

                String instruction = "📸 Iltimos, mushukning rasmlarini yuboring:\n\n" +
                        " • Mushukchani chiroyli suratidan jo'nating \n" +
                        " • 1 dan 3 tagacha bo'lgan surat jo'natishingiz mumkin\n" +
                        " • yoki 5-10 sekundgacha video jo'ylashingiz mumkin 10 sekuntdan\n\n" +
                        " • ortiq videoni qabul qilmaymiz ❗️\uFE0F";

                sendText(chatId, instruction);
                return;
            }

            switch (data) {
                case "menu_reklama":
                    sendAdTypeSelection(chatId);
                    break;
                case "menu_admin":
                    sendText(chatId, "👤 Admin bilan bog'lanish:\n\n📶 @zayd_catlover\n\n📞 +998934938181");
                    break;
                case "menu_narx":
                    sendPriceList(chatId);
                    break;
                case "menu_konkurs":
                    sendKonkursMenu(chatId);
                    break;
                case "menu_yordam":
                    sendYordamMenu(chatId);
                    break;

                // Konkurs menyusi
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
                    sendMainMenu(chatId);
                    break;

                // Admin paneli
                case "admin_panel":
                    sendAdminPanel(chatId);
                    break;
                case "admin_stats":
                    sendAdminStatisticsMenu(chatId);
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

                // Bot haqida menyusi
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

                // Reklama turi
                case "adtype_sotish":
                    adTypeMap.put(chatId, "sotish");
                    startAdProcess(chatId);
                    break;
                case "adtype_hadiya":
                    adTypeMap.put(chatId, "hadiya");
                    startAdProcess(chatId);
                    break;
                case "adtype_vyazka":
                    adTypeMap.put(chatId, "vyazka");
                    sendPlatformaSelection(chatId);
                    break;
                case "adtype_back":
                    sendMainMenu(chatId);
                    break;

                // Davom etish tugmasi
                case "continue_process":
                    handleContinueProcess(chatId);
                    break;

                // MUSHUK SONI TANLASH
                case "mushuk_1":
                    System.out.println("Mushuk 1 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 1);
                    break;
                case "mushuk_2":
                    System.out.println("Mushuk 2 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 2);
                    break;
                case "mushuk_3":
                    System.out.println("Mushuk 3 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 3);
                    break;
                case "mushuk_4":
                    System.out.println("Mushuk 4 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 4);
                    break;
                case "mushuk_5":
                    System.out.println("Mushuk 5 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 5);
                    break;
                case "mushuk_kop":
                    System.out.println("Mushuk +5 tanlandi: " + chatId);
                    handleMushukSoni(chatId, 6);
                    break;

                // Custom breed tanlash
                case "breed_custom":
                    stateMap.put(chatId, "await_custom_breed");
                    sendText(chatId, "✏️ Iltimos, mushukingiz zotini yozing:");
                    break;

                // Yordam menyusi
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

                // Viloyat tanlash (reklama uchun)
                case "viloyat_andijon": case "viloyat_buxoro": case "viloyat_fargona":
                case "viloyat_jizzax": case "viloyat_xorazm": case "viloyat_namangan":
                case "viloyat_navoiy": case "viloyat_qashqadaryo": case "viloyat_samarqand":
                case "viloyat_sirdaryo": case "viloyat_surxondaryo": case "viloyat_toshkent":
                case "viloyat_toshkent_shahar":
                    String viloyat = data.replace("viloyat_", "").replace("_", " ");
                    manzilMap.put(chatId, viloyat);
                    stateMap.put(chatId, "await_phone");
                    sendText(chatId, "📍 Manzil: " + viloyat + "\n📞 Endi telefon raqamingizni yuboring: (masalan +998 90 123 45 67)");
                    break;

                // Yordam uchun viloyat tanlash
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

                // Yordam telefon raqam qabul qilish
                case "yordam_phone_confirm":
                    String currentState = stateMap.get(chatId);
                    if (currentState.startsWith("yordam_")) {
                        stateMap.put(chatId, currentState + "_photo");
                        sendText(chatId, "📸 Endi rasm yuboring (1-3 ta rasm yoki 10 soniyagacha video):");
                    }
                    break;

                // Yosh tanlash
                case "age_1_oylik": case "age_2_oylik": case "age_3_oylik": case "age_4_oylik":
                case "age_5_oylik": case "age_6_oylik": case "age_7_oylik": case "age_8_oylik":
                case "age_9_oylik": case "age_10_oylik": case "age_11_oylik": case "age_1_yosh":
                    String age = data.replace("age_", "").replace("_", " ");
                    ageMap.put(chatId, age);

                    if ("vyazka".equals(adTypeMap.get(chatId))) {
                        sendGenderSelection(chatId);
                    } else {
                        sendHealthSelection(chatId);
                    }
                    break;

                // Sog'lik tanlash
                case "health_soglom":
                    healthMap.put(chatId, "Sog'lom");
                    sendGenderSelection(chatId);
                    break;
                case "health_kasal":
                    healthMap.put(chatId, "Kasal");
                    sendGenderSelection(chatId);
                    break;

                // JINS TANLASH
                case "gender_qiz":
                    genderMap.put(chatId, "Qiz bola");
                    sendSterilizationSelection(chatId);
                    break;
                case "gender_ogil":
                    genderMap.put(chatId, "O'g'il bola");
                    sendSterilizationSelection(chatId);
                    break;

                // NASL OLISH TANLASH
                case "sterilization_yes":
                    sterilizationMap.put(chatId, "Nasl olish mumkin");
                    if ("sotish".equals(adTypeMap.get(chatId)) || "vyazka".equals(adTypeMap.get(chatId))) {
                        sendMushukSoniSelection(chatId);
                    } else {
                        sendPreview(chatId);
                    }
                    break;
                case "sterilization_no":
                    sterilizationMap.put(chatId, "Nasl olish mumkin emas");
                    if ("sotish".equals(adTypeMap.get(chatId)) || "vyazka".equals(adTypeMap.get(chatId))) {
                        sendMushukSoniSelection(chatId);
                    } else {
                        sendPreview(chatId);
                    }
                    break;

                // Preview tugmalari
                case "preview_confirm":
                    if ("sotish".equals(adTypeMap.get(chatId)) || "vyazka".equals(adTypeMap.get(chatId))) {
                        if (priceMap.containsKey(chatId) && priceMap.get(chatId) != null && !priceMap.get(chatId).trim().isEmpty()) {
                            sendPaymentInstructions(chatId);
                        } else {
                            stateMap.put(chatId, "await_price");
                            sendText(chatId, "💰 Mushukchangizni nech pulga sotmoqchisiz? \n" +
                                    "\n" +
                                    "Eslatma bozor narxlarni hissobga olgan xolatda, mushugingizga mos narx qo'ying. Sizga xam sotib oluvchi mijozga xam maqul bo'ladigan narx qo'ying Alloh barakasini bersin .\n"+
                                    "Masalan:100.000 so'm yoki 100$ da qiling "+
                                    "iltimos narxi yozyotkanda tuliq yozing");
                        }
                    } else {
                        sendText(chatId, "✅ Ma'lumotlaringiz qabul qilindi! Admin tekshirib kanalga joylaydi.");
                        notifyAdmin(chatId);
                        stateMap.put(chatId, "waiting_admin");
                    }
                    break;
                case "preview_back":
                    stateMap.put(chatId, "await_photo");
                    photosMap.remove(chatId);
                    sendText(chatId, "↩️ Orqaga qaytildi. Iltimos, rasmlarni qayta yuboring yoki /start ni bosing.");
                    break;

                // Admin tasdiqlash va o'zgartirish
                case "admin_edit_breed":
                    handleAdminEditBreed(chatId);
                    break;
                case "admin_edit_confirm":
                    handleAdminEditConfirm(chatId);
                    break;
                case "admin_edit_cancel":
                    sendAdminPanel(chatId);
                    break;

                // DEFAULT qismi - Admin tasdiqlash
                default:
                    if (data.startsWith("approve_")) {
                        if (fromId == ADMIN_ID) {
                            String uidStr = data.substring("approve_".length());
                            long uid = Long.parseLong(uidStr);

                            postToChannel(uid);
                            sendText(uid, "✅ E'loningiz kanalga joylandi!");
                            deleteAdminMessages(uid);
                            sendText(ADMIN_ID, "✅ E'lon tasdiqlandi va kanalga joylandi. Xabarlar tozalandi.");
                        }
                    } else if (data.startsWith("decline_")) {
                        if (fromId == ADMIN_ID) {
                            String uidStr = data.substring("decline_".length());
                            long uid = Long.parseLong(uidStr);

                            stateMap.put(ADMIN_ID, "admin_decline_reason_" + uid);
                            sendText(ADMIN_ID, "📝 Foydalanuvchiga yuborish uchun rad etish sababini yozing:");
                        }
                    } else if (data.startsWith("edit_")) {
                        if (fromId == ADMIN_ID) {
                            String uidStr = data.substring("edit_".length());
                            long uid = Long.parseLong(uidStr);
                            adminEditUserIdMap.put(ADMIN_ID, uid);
                            sendAdminEditMenu(chatId, uid);
                        }
                    } else if (data.startsWith("yordam_approve_")) {
                        if (fromId == ADMIN_ID) {
                            String uidStr = data.substring("yordam_approve_".length());
                            long uid = Long.parseLong(uidStr);
                            postYordamToChannel(uid);
                            sendText(uid, "✅ So'rovingiz tasdiqlandi va kanalga joylandi!");
                            sendText(ADMIN_ID, "✅ Yordam so'rovi tasdiqlandi.");
                        }
                    } else if (data.startsWith("yordam_decline_")) {
                        if (fromId == ADMIN_ID) {
                            String uidStr = data.substring("yordam_decline_".length());
                            long uid = Long.parseLong(uidStr);
                            sendText(uid, "❌ So'rovingiz tasdiqlanmadi. Admin bilan bog'laning.");
                            sendText(ADMIN_ID, "❌ Yordam so'rovi rad etildi.");
                        }
                    }
                    break;
            }
        }

        // ========== ADMIN KONKURS RASM O'ZGARTIRISH ==========
        private void handleAdminKonkursImage(long adminId) throws TelegramApiException {
            stateMap.put(adminId, "admin_await_konkurs_image");
            sendText(adminId, "🖼️ Iltimos, yangi konkurs rasmini yuboring (faqat rasm):");
        }

        // ========== ASOSIY MENYU ==========
        private void sendMainMenu(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🐱 Assalomu alaykum! UzbekCats botiga xush kelibsiz!\n\nQuyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton b1 = new InlineKeyboardButton();
            b1.setText("📢 Reklama joylash");
            b1.setCallbackData("menu_reklama");
            rows.add(Collections.singletonList(b1));

            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("🏆 Konkurs");
            b2.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(b2));

            InlineKeyboardButton b3 = new InlineKeyboardButton();
            b3.setText("👤 Admin bilan bog'lanish");
            b3.setCallbackData("menu_admin");
            rows.add(Collections.singletonList(b3));

            if (chatId == ADMIN_ID) {
                InlineKeyboardButton adminBtn = new InlineKeyboardButton();
                adminBtn.setText("👨‍💼 Admin paneli");
                adminBtn.setCallbackData("admin_panel");
                rows.add(Collections.singletonList(adminBtn));
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // ========== KONKURS METODLARI ==========
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

        private void sendKonkursMukofot(long chatId) throws TelegramApiException {
            try {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(chatId));
                photo.setPhoto(new InputFile(currentKonkursImageUrl));
                photo.setCaption(currentKonkursText);
                execute(photo);
            } catch (Exception e) {
                // Agar rasm yuklashda xatolik bo'lsa, faqat matnni yuboramiz
                sendText(chatId, currentKonkursText);
            }
        }

        private void sendKonkursRating(long chatId) throws TelegramApiException {
            StringBuilder ratingText = new StringBuilder();
            ratingText.append("🏆 TOP ISHTIROKCHILAR:\n\n");

            for (int i = 0; i < konkursParticipants.size(); i++) {
                KonkursParticipant participant = konkursParticipants.get(i);

                String medal = "";
                if (i == 0) medal = "🥇";
                else if (i == 1) medal = "🥈";
                else if (i == 2) medal = "🥉";
                else medal = "👤";

                ratingText.append(medal).append(" ").append(participant.name);

                if (!participant.username.isEmpty()) {
                    ratingText.append(" (").append(participant.username).append(")");
                }

                ratingText.append("\nBall: ").append(participant.score).append(" 🎯\n\n");
            }

            ratingText.append("📊 Sizning ballingiz: 0 🎯");

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(ratingText.toString());

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = 0; i < Math.min(5, konkursParticipants.size()); i++) {
                KonkursParticipant participant = konkursParticipants.get(i);
                if (!participant.profileLink.isEmpty()) {
                    InlineKeyboardButton btn = new InlineKeyboardButton();
                    btn.setText((i + 1) + ". " + participant.name);
                    btn.setUrl(participant.profileLink);
                    rows.add(Collections.singletonList(btn));
                }
            }

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_konkurs");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            msg.disableWebPagePreview();
            execute(msg);
        }

        private void sendKonkursShartlar(long chatId) throws TelegramApiException {
            String shartlarText = "⬇️ Qatnashish uchun:\n\n" +
                    "🔗 Bot sizga taqdim etgan referral linkni iloji boricha ko'proq do'stlaringizga ulashing. " +
                    "Sizni linkingizdan qo'shilgan har bir ishtirokchiga 1 balldan beriladi. " +
                    "Sovg'alar eng ko'p ball to'plagan ishtirokchiga beriladi. " +
                    "Konkurs har xafta bo'ladi va xaftaning juma kuni 1-o'rinda turgan ishtirokchimiz g'olib bo'ladi va kanal linkini tashlaydi.\n\n"+
                    "\uD83D\uDCDA  KONKURSIDA ISHTIROK ETING!\n" +
                    "\n" +
                    "\uD83C\uDF81 Mukofotlar:\n" +
                    "\uD83E\uDD47 Scottish fold black\n" +
                    "Siz toplagan ovoz ochib ketmaydi toki \uD83C\uDFC6 g'olib bo'lguningizga qadar \uD83D\uDCAF\n" +
                    "\n" +
                    "✅ Qatnashish juda oson:\n" +
                    "1. Botga start bosing\n" +
                    "2. Kanallarga a'zo bo'ling\n" +
                    "3. Do'stlaringizni taklif qiling\n" +
                    "4. Eng ko'p ball to'plab, mukofotlarni qo'lga kiriting!\n" +
                    "\n" +
                    "⚡\uFE0F Shoshiling, sizdan avval sovg'alarga ega chiqib qo'yishmasin \uD83D\uDE09\n" +
                    "\n" +
                    "\uD83D\uDD17 Ishtirok etish uchun: https://t.me/Uzbekcatsbot?start=7038296036";

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(shartlarText);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Orqaga");
            backBtn.setCallbackData("menu_konkurs");

            markup.setKeyboard(Collections.singletonList(Collections.singletonList(backBtn)));
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // ========== REKLAMA TURI TANLASH ==========
        private void sendAdTypeSelection(long chatId) throws TelegramApiException {
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
            b5.setText("💰 Narxlar");
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

        private void startAdProcess(long chatId) throws TelegramApiException {
            stateMap.put(chatId, "await_photo");
            photosMap.put(chatId, new ArrayList<>());

            String instruction = "📸 Iltimos, mushukning rasmlarini yuboring:\n\n" +
                    " • Mushukchani chiroyli suratidan jo'nating \n" +
                    " • 1 dan 3 tagacha bo'lgan surat jo'natishingiz mumkin\n" +
                    " • yoki 5-10 sekundgacha video jo'ylashingiz mumkin 10 sekuntdan\n\n" +
                    " • ortiq videoni qabul qilmaymiz ❗️\uFE0F";

            sendText(chatId, instruction);
        }

        // ========== VILOYAT TANLASH ==========
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

        // ========== YOSH TANLASH ==========
        private void sendAgeSelection(long chatId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🎂 Yoshini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = 0; i < ages.size(); i += 3) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                for (int j = i; j < Math.min(i + 3, ages.size()); j++) {
                    InlineKeyboardButton btn = new InlineKeyboardButton();
                    btn.setText(ages.get(j));
                    btn.setCallbackData("age_" + ages.get(j).toLowerCase().replace(" ", "_").replace("+", "").replace(".", "_"));
                    row.add(btn);
                }
                rows.add(row);
            }

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        // ========== SOG'LIK TANLASH ==========
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

        // ========== JINS TANLASH ==========
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

        // ========== NASL OLISH TANLASH ==========
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

        // ========== PLATFORMA TANLASH (VYAZKA UCHUN) ==========
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

        // ========== ZOT TANLASH ==========
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

        // ========== MUSHUK SONI TANLASH ==========
        private void handleMushukSoni(long chatId, int soni) throws TelegramApiException {
            System.out.println("handleMushukSoni called: " + soni + " ta, chatId: " + chatId);
            mushukSoniMap.put(chatId, soni);
            sendPreview(chatId);
        }

        private void sendMushukSoniSelection(long chatId) throws TelegramApiException {
            System.out.println("sendMushukSoniSelection called: " + chatId);

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

        // ========== PREVIEW KO'RSATISH ==========
        private void sendPreview(long chatId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);
            if (userPhotos == null || userPhotos.isEmpty()) {
                sendText(chatId, "Xatolik: Rasmlar topilmadi.");
                return;
            }

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

                if (!"hadiya".equals(adType)) {
                    String narx = priceMap.getOrDefault(chatId, "—");
                    sb.append("💰 Narx: ").append(narx).append(" so'm\n");
                }
            }

            sb.append("\nMa'lumotlaringiz to'g'rimi?");
            System.out.println("Preview caption: " + sb.toString());

            return sb.toString();
        }

        // ========== TO'LOV KO'RSATMALARI ==========
        private void sendPaymentInstructions(long chatId) throws TelegramApiException {
            int mushukSoni = mushukSoniMap.getOrDefault(chatId, 1);
            int narx = 0;
            String mushukText = "";

            System.out.println("To'lov hisoblash: " + mushukSoni + " ta mushuk");

            switch (mushukSoni) {
                case 1:
                    narx = 35000;
                    mushukText = "1 ta mushuk";
                    break;
                case 2:
                    narx = 70000;
                    mushukText = "2 ta mushuk";
                    break;
                case 3:
                    narx = 105000;
                    mushukText = "3 ta mushuk";
                    break;
                case 4:
                    narx = 120000;
                    mushukText = "4 ta mushuk";
                    break;
                case 5:
                    narx = 150000;
                    mushukText = "5 ta mushuk";
                    break;
                case 6:
                    narx = 150000;
                    mushukText = "+5 ta mushuk";
                    break;
                default:
                    narx = 35000;
                    mushukText = "1 ta mushuk";
                    break;
            }

            String message = "💳 To'lov ma'lumotlari:\n\n" +
                    "Siz " + mushukText + " uchun to'lov qilishingiz kerak:\n\n" +
                    "💵 To'lov miqdori: " + String.format("%,d", narx) + " so'm\n" +
                    "💳 Karta raqam: `5614681626280956`\n" +
                    "👤 Karta egasi: Xalilov.A\n\n" +
                    "To'lov qilib, chekni rasmini yuboring.";

            System.out.println("To'lov xabari: " + message);
            sendText(chatId, message);
            stateMap.put(chatId, "wait_check");
        }

        // ========== DAVOM ETISH TUGMASI ==========
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

        private void handleContinueProcess(long chatId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);

            if (userPhotos == null || userPhotos.isEmpty()) {
                sendText(chatId, "❌ Iltimos, kamida 1 ta rasm yuboring!");
                return;
            }

            sendViloyatSelection(chatId);
        }

        // ========== YORDAM MENYUSI ==========
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

        // ========== YORDAM VILOYAT TANLASH ==========
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

        // ========== ADMIN PANELI ==========
        private void sendAdminPanel(long chatId) throws TelegramApiException {
            if (chatId != ADMIN_ID) {
                sendText(chatId, "❌ Siz admin emassiz!");
                return;
            }

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("👨‍💼 Admin paneliga xush kelibsiz!\n\nQuyidagilardan birini tanlang:");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            InlineKeyboardButton statsBtn = new InlineKeyboardButton();
            statsBtn.setText("📊 Statistika");
            statsBtn.setCallbackData("admin_stats");
            rows.add(Collections.singletonList(statsBtn));

            // Yangi tugma - konkurs rasm o'zgartirish
            InlineKeyboardButton konkursImageBtn = new InlineKeyboardButton();
            konkursImageBtn.setText("🖼️ Konkurs rasmini o'zgartirish");
            konkursImageBtn.setCallbackData("admin_konkurs_image");
            rows.add(Collections.singletonList(konkursImageBtn));

            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("↩️ Asosiy menyu");
            backBtn.setCallbackData("adtype_back");
            rows.add(Collections.singletonList(backBtn));

            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            execute(msg);
        }

        private void sendAdminStatisticsMenu(long chatId) throws TelegramApiException {
            if (chatId != ADMIN_ID) {
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
                    String username = record.username != null ? "@" + record.username : "ID: " + record.userId;
                    statsText.append("*").append(i + 1).append(".* ").append(record.breed)
                            .append("\n   👤 ").append(username)
                            .append("\n   📞 ").append(record.phone)
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

        // ========== ADMIN O'ZGARTIRISH METODLARI ==========
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

        private void handleAdminSetBreed(long adminId, String data) throws TelegramApiException {
            Long userId = adminEditUserIdMap.get(adminId);
            if (userId == null) return;

            String breed = data.replace("admin_set_breed_", "").replace("_", " ");
            breedMap.put(userId, breed);

            sendText(adminId, "✅ Zot o'zgartirildi: " + breed);
            sendAdminEditMenu(adminId, userId);
        }

        private void handleAdminEditBreed(long adminId) throws TelegramApiException {
            sendAdminBreedSelection(adminId);
        }

        private void handleAdminEditConfirm(long adminId) throws TelegramApiException {
            Long userId = adminEditUserIdMap.get(adminId);
            if (userId == null) return;

            postToChannel(userId);
            sendText(userId, "✅ E'loningiz kanalga joylandi!");
            sendText(adminId, "✅ E'lon o'zgartirildi va kanalga joylandi!");

            deleteAdminMessages(userId);
            adminEditUserIdMap.remove(adminId);
        }

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

        private void sendAdminEditMenu(long adminId, long userId) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminId));
            msg.setText("✏️ O'zgartirmoqchi bo'lgan maydonni tanlang:\n\n" + buildAdminPreviewCaption(userId));

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            String username = userUsernameMap.getOrDefault(userId, "user_" + userId);
            InlineKeyboardButton contactBtn = new InlineKeyboardButton();
            contactBtn.setText("👥 Obunachi bilan aloqa");
            contactBtn.setUrl("https://t.me/" + username);
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

        // ========== NARXLAR RO'YXATI ==========
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

        // ========== TELEFON RAQAM TEKSHIRISH ==========
        private boolean isValidPhoneNumber(String phone) {
            String regex = "^\\+998\\s\\d{2}\\s\\d{3}\\s\\d{2}\\s\\d{2}$";
            if (!phone.matches(regex)) {
                String digitsOnly = phone.replaceAll("[^0-9]", "");
                return digitsOnly.length() >= 12 && digitsOnly.startsWith("998") && digitsOnly.substring(3).length() == 9;
            }
            return true;
        }

        // ========== KANALGA JOYLASH ==========
        private void postToChannel(long userId) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(userId);
            if (userPhotos == null || userPhotos.isEmpty()) {
                System.out.println("❌ Rasmlar topilmadi!");
                return;
            }

            // Statistika saqlash
            String adType = adTypeMap.getOrDefault(userId, "");
            saveStatistics(userId, adType);

            List<String> photos = new ArrayList<>();
            String videoFileId = null;

            for (String media : userPhotos) {
                if (media.startsWith("video:")) {
                    videoFileId = media.substring(6);
                } else {
                    photos.add(media);
                }
            }

            if (videoFileId != null) {
                sendVideoToChannel(userId, videoFileId, photos);
                return;
            }

            if (!photos.isEmpty()) {
                List<String> watermarkedPhotos = new ArrayList<>();

                for (String photoId : photos) {
                    if (watermarkedPhotos.size() < 3) {
                        String watermarkedFileId = addWatermarkToImage(photoId);
                        if (watermarkedFileId != null) {
                            watermarkedPhotos.add(watermarkedFileId);
                        } else {
                            watermarkedPhotos.add(photoId);
                        }
                    }
                }

                sendPhotosToChannel(userId, watermarkedPhotos);
            }
        }

        private void sendVideoToChannel(long userId, String videoFileId, List<String> photos) throws TelegramApiException {
            String caption = buildChannelCaption(userId, adTypeMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""), phoneMap.getOrDefault(userId, ""));

            if (!photos.isEmpty()) {
                SendPhoto photoMsg = new SendPhoto();
                photoMsg.setChatId(CHANNEL_USERNAME);
                photoMsg.setPhoto(new InputFile(photos.get(0)));
                photoMsg.setCaption(caption);
                photoMsg.setParseMode("Markdown");
                execute(photoMsg);

                for (int i = 1; i < photos.size(); i++) {
                    SendPhoto additionalPhoto = new SendPhoto();
                    additionalPhoto.setChatId(CHANNEL_USERNAME);
                    additionalPhoto.setPhoto(new InputFile(photos.get(i)));
                    execute(additionalPhoto);
                }

                SendVideo videoMsg = new SendVideo();
                videoMsg.setChatId(CHANNEL_USERNAME);
                videoMsg.setVideo(new InputFile(videoFileId));
                execute(videoMsg);

            } else {
                SendVideo videoMsg = new SendVideo();
                videoMsg.setChatId(CHANNEL_USERNAME);
                videoMsg.setVideo(new InputFile(videoFileId));
                videoMsg.setCaption(caption);
                videoMsg.setParseMode("Markdown");
                execute(videoMsg);
            }
        }

        private void sendPhotosToChannel(long userId, List<String> photos) throws TelegramApiException {
            String caption = buildChannelCaption(userId, adTypeMap.getOrDefault(userId, ""),
                    manzilMap.getOrDefault(userId, ""), phoneMap.getOrDefault(userId, ""));

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
                execute(mediaGroup);
            } else if (photos.size() == 1) {
                SendPhoto post = new SendPhoto();
                post.setChatId(CHANNEL_USERNAME);
                post.setPhoto(new InputFile(photos.get(0)));
                post.setCaption(caption);
                post.setParseMode("Markdown");
                execute(post);
            }
        }

        private String buildChannelCaption(long userId, String adType, String manzil, String phone) {
            StringBuilder caption = new StringBuilder();

            String breed = breedMap.getOrDefault(userId, "");
            String age = ageMap.getOrDefault(userId, "");
            String gender = genderMap.getOrDefault(userId, "");
            String health = healthMap.getOrDefault(userId, "");
            String sterilization = sterilizationMap.getOrDefault(userId, "");

            if ("hadiya".equals(adType)) {
                caption.append("#HADIYAGA 🎁\n\n");
                caption.append("📝 Mushukcha yaxshi insonlarga tekinga sovg'a qilinadi. Iltimos mushukni sotadigan yoki chidolmay ko'chaga tashlab ketadigan bo'lsangiz olmang! Allohdan qo'rqing\n\n");
                caption.append("🏠 Manzil: ").append(manzil).append("\n");
                caption.append("📞 Nomer: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("\uD83D\uDCFD\uFE0F [YouTube](https://youtu.be/vdwgSB7_amw)  ");
                caption.append("\uD83C\uDF10 [Instagram](https://www.instagram.com/p/C-cZkgstVGK/)  ");
                caption.append("✉\uFE0F [Telegram](https://t.me/uzbek_cats)");

            } else if ("vyazka".equals(adType)) {
                caption.append("#VYAZKAGA 💝\n\n");
                caption.append("📝").append(breed).append(" ").append(age).append(" ").append(gender.toLowerCase())
                        .append(" ").append(sterilization).append(" ").append(health.toLowerCase()).append("\n\n");
                caption.append("📍 Manzil: ").append(manzil).append("\n");
                caption.append("💵 Narxi: ").append(priceMap.getOrDefault(userId, "")).append(" so'm\n");
                caption.append("📞 Tel: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("\uD83D\uDCFD\uFE0F [YouTube](https://youtu.be/vdwgSB7_amw)  ");
                caption.append("\uD83C\uDF10 [Instagram](https://www.instagram.com/p/C-cZkgstVGK/)  ");
                caption.append("✉\uFE0F [Telegram](https://t.me/uzbek_cats)");

            } else {
                caption.append("#SOTILADI 💰\n\n");
                caption.append("📝").append(breed).append(" ").append(age).append(" ").append(gender.toLowerCase())
                        .append(" ").append(sterilization).append(" ").append(health.toLowerCase()).append("\n\n");
                caption.append("📍 Manzil: ").append(manzil).append("\n");
                caption.append("💵 Narxi: ").append(priceMap.getOrDefault(userId, "")).append(" so'm\n");
                caption.append("📞 Tel: ").append(phone).append("\n\n");
                caption.append("👤 [Admin](https://t.me/zayd_catlover)\n");
                caption.append("📢 [Reklama berish uchun](https://t.me/Uzbek_cat_bot").append("?start=reklama)\n\n");
                caption.append("\uD83D\uDCFD\uFE0F [YouTube](https://youtu.be/vdwgSB7_amw)  ");
                caption.append("\uD83C\uDF10 [Instagram](https://www.instagram.com/p/C-cZkgstVGK/)  ");
                caption.append("✉\uFE0F [Telegram](https://t.me/uzbek_cats)");
            }

            return caption.toString();
        }

        // ========== WATERMARK QO'SHISH ==========
        private String addWatermarkToImage(String fileId) {
            try {
                String fileUrl = getFileUrl(fileId);
                URL url = new URL(fileUrl);
                BufferedImage originalImage = ImageIO.read(url);

                BufferedImage watermarkedImage = new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

                Graphics2D g2d = watermarkedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, null);

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // FAQAT LOGO QISMINI O'CHIRDIK (quyidagi qatorlar olib tashlandi)
                // URL logoUrl = new URL(LOGO_URL);
                // BufferedImage originalLogo = ImageIO.read(logoUrl);
                // int logoSize = originalImage.getWidth() / 17;
                // BufferedImage circularLogo = createCircularImage(originalLogo, logoSize);
                // int logoX = 10;
                // int logoY = 10;
                // g2d.drawImage(circularLogo, logoX, logoY, null);

                // "UzbekCats" yozuvini SAQLADIK
                g2d.setFont(new Font("Arial", Font.BOLD, 25));
                g2d.setColor(Color.YELLOW);

                String watermarkText = "@UzbekCats";
                FontMetrics metrics = g2d.getFontMetrics();
                int textX = 10; // Logosiz, to'g'ridan-to'g'ri chap tomondan boshlaymiz
                int textY = 30; // Yuqori chetga joylashtiramiz

                g2d.drawString(watermarkText, textX, textY);
                g2d.dispose();

                File outputFile = new File("watermarked_" + System.currentTimeMillis() + ".jpg");
                ImageIO.write(watermarkedImage, "jpg", outputFile);

                String newFileId = uploadPhotoToTelegram(outputFile);
                outputFile.delete();

                return newFileId;

            } catch (Exception e) {
                System.out.println("❌ Watermark qo'shishda xatolik: " + e.getMessage());
                return fileId;
            }
        }

        private BufferedImage createCircularImage(BufferedImage originalImage, int size) {
            BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = circularImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
            g2d.setClip(circle);

            g2d.drawImage(originalImage, 0, 0, size, size, null);

            g2d.setClip(null);
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.WHITE);
            g2d.drawOval(0, 0, size-1, size-1);

            g2d.dispose();
            return circularImage;
        }

        private String getFileUrl(String fileId) throws TelegramApiException {
            org.telegram.telegrambots.meta.api.objects.File file = execute(
                    new org.telegram.telegrambots.meta.api.methods.GetFile(fileId)
            );
            return "https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + file.getFilePath();
        }

        private String uploadPhotoToTelegram(File file) throws TelegramApiException {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(ADMIN_ID));
            sendPhoto.setPhoto(new InputFile(file));

            Message message = execute(sendPhoto);
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                return photos.get(photos.size()-1).getFileId();
            }
            return null;
        }

        // ========== STATISTIKANI SAQLASH ==========
        private void saveStatistics(long userId, String adType) {
            String breed = breedMap.getOrDefault(userId, "");
            String phone = phoneMap.getOrDefault(userId, "");
            String username = userUsernameMap.getOrDefault(userId, "user_" + userId);

            AdRecord record = new AdRecord(userId, username, adType, breed, phone);
            statisticsMap.get(adType).add(record);
        }

        // ========== ADMINGA XABAR YUBORISH ==========
        private void notifyAdminForYordam(long chatId, String type) throws TelegramApiException {
            List<String> userPhotos = photosMap.get(chatId);

            String adminText = "\uD83D\uDE91 YANGI YORDAM SO'ROVI\n\n";
            adminText += "Turi: " + type + "\n";
            adminText += "User ID: " + chatId + "\n";
            adminText += "Manzil: " + manzilMap.getOrDefault(chatId, "—") + "\n";
            adminText += "Telefon: " + phoneMap.getOrDefault(chatId, "—") + "\n\n";
            adminText += "Tasdiqlaysizmi?";

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

        // ========== ADMINGA XABAR YUBORISH ==========
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
                SendPhoto photo = new SendPhoto();
                photo.setChatId(String.valueOf(ADMIN_ID));
                photo.setPhoto(new InputFile(userPhotos.get(0)));
                photo.setCaption("E'lon rasmi - ID: " + adId);

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
                photo.setReplyMarkup(markup);

                Message photoMessage = execute(photo);
                adminMessageIds.put(photoMessage.getMessageId(), chatId);
            }

            if (checkMap.containsKey(chatId)) {
                SendPhoto check = new SendPhoto();
                check.setChatId(String.valueOf(ADMIN_ID));
                check.setPhoto(new InputFile(checkMap.get(chatId)));
                check.setCaption("💳 To'lov cheki - ID: " + adId);
                Message checkMessage = execute(check);
                adminMessageIds.put(checkMessage.getMessageId(), chatId);
            }
        }

        // ========== ADMIN XABARLARNI O'CHIRISH ==========
        private void deleteAdminMessages(long userId) {
            try {
                System.out.println("🗑️ Foydalanuvchi ma'lumotlari o'chirilmoqda: " + userId);

                // 1. FOYDALANUVCHI MA'LUMOTLARINI TOZALASH
                photosMap.remove(userId);
                manzilMap.remove(userId);
                phoneMap.remove(userId);
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
                stateMap.remove(userId);
                declineReasonsMap.remove(userId);

                // 2. ✅ ADMIN PANELIDAGI XABARLARNI O'CHIRISH
                deleteAdminPanelMessages(userId);

                System.out.println("✅ Barcha ma'lumotlar va admin xabarlari o'chirildi: " + userId);

            } catch (Exception e) {
                System.out.println("❌ Xabarlarni o'chirishda xatolik: " + e.getMessage());
            }
        }

        // ✅ ADMIN PANELIDAGI XABARLARNI O'CHIRISH
        private void deleteAdminPanelMessages(long userId) {
            try {
                List<Integer> messagesToDelete = new ArrayList<>();

                // Admin xabarlarini topish
                for (Map.Entry<Integer, Long> entry : adminMessageIds.entrySet()) {
                    if (entry.getValue() == userId) {
                        messagesToDelete.add(entry.getKey());
                    }
                }

                // Xabarlarni o'chirish
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

        // ========== YORDAM METODLARI ==========
        private void sendText(long chatId, String text) throws TelegramApiException {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(text);
            execute(msg);
        }
    }
}// Mushukchangizni