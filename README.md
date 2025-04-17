# androidapp_learning_english

**cấu trúc thư mục dự án**
```text
learning_english/
│   ├── app
│   │   ├── sampledata
│   │   ├── src
│   │   │   ├── androidTest
│   │   │   │   └── java
│   │   │   │       └── com
│   │   │   │           └── example
│   │   │   │               └── learning_english
│   │   │   │                   └── ExampleInstrumentedTest.java
│   │   │   ├── main
│   │   │   │   ├── assets
│   │   │   │   │   ├── database
│   │   │   │   │   │   └── dictionary.db
│   │   │   │   │   └── grammar_theory
│   │   │   │   │       ├── 1. Kiến thức cơ bản 1  từ loại và cụm từ.json
│   │   │   │   │       ├── 10. Danh động từ.json
│   │   │   │   │       ├── 11. Phân từ và cấu trúc phân từ.json
│   │   │   │   │       ├── 12. Trạng từ.json
│   │   │   │   │       ├── 13. Giới từ.json
│   │   │   │   │       ├── 14. Liên từ.json
│   │   │   │   │       ├── 15. Mệnh đề quan hệ.json
│   │   │   │   │       ├── 16. Câu điều kiện.json
│   │   │   │   │       ├── 17. Cấu trúc so sánh.json
│   │   │   │   │       ├── 2. Kiến thức cơ bản 2 Mệnh đề và câu.json
│   │   │   │   │       ├── 3. Danh từ.json
│   │   │   │   │       ├── 4. Đại từ.json
│   │   │   │   │       ├── 5. Tính từ.json
│   │   │   │   │       ├── 6. Thì.json
│   │   │   │   │       ├── 7. Thể.json
│   │   │   │   │       ├── 8. Động từ nguyên mẫu.json
│   │   │   │   │       └── 9. Động từ nguyên mẫu có 'to'.json
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   │       └── example
│   │   │   │   │           └── learning_english
│   │   │   │   │               ├── activity
│   │   │   │   │               │   ├── FlashcardActivity.java
│   │   │   │   │               │   ├── MainActivity.java
│   │   │   │   │               │   ├── StoryActivity.java
│   │   │   │   │               │   ├── StoryActivity.java~
│   │   │   │   │               │   └── VocabularyListActivity.java
│   │   │   │   │               ├── adapter
│   │   │   │   │               │   ├── GrammarDetailAdapter.java
│   │   │   │   │               │   ├── GrammarListAdapter.java
│   │   │   │   │               │   ├── PracticeTopicAdapter.java
│   │   │   │   │               │   ├── PracticeTopicAdapter.java~
│   │   │   │   │               │   ├── SimpleStringListAdapter.java
│   │   │   │   │               │   └── WordListAdapter.java
│   │   │   │   │               ├── db
│   │   │   │   │               │   ├── AppDatabase.java
│   │   │   │   │               │   ├── DictionaryDao.java
│   │   │   │   │               │   ├── DictionaryEntry.java
│   │   │   │   │               │   ├── Word.java
│   │   │   │   │               │   └── WordDao.java
│   │   │   │   │               ├── fragment
│   │   │   │   │               │   ├── BaseQuizFragment.java
│   │   │   │   │               │   ├── BaseQuizFragment.java~
│   │   │   │   │               │   ├── GrammarDetailFragment.java
│   │   │   │   │               │   ├── GrammarExerciseFragment.java
│   │   │   │   │               │   ├── GrammarExerciseFragment.java~
│   │   │   │   │               │   ├── GrammarFragment.java
│   │   │   │   │               │   ├── GrammarPracticeListFragment.java
│   │   │   │   │               │   ├── GrammarPracticeListFragment.java~
│   │   │   │   │               │   ├── GrammarResultFragment.java
│   │   │   │   │               │   ├── GrammarTheoryListFragment.java
│   │   │   │   │               │   ├── PracticeFragment.java
│   │   │   │   │               │   ├── ToeicPart5Fragment.java
│   │   │   │   │               │   ├── ToeicPart5Fragment.java~
│   │   │   │   │               │   ├── VocabularyFragment.java
│   │   │   │   │               │   └── VocabularyFragment.java~
│   │   │   │   │               ├── model
│   │   │   │   │               │   ├── grammar
│   │   │   │   │               │   │   ├── ContentItem.java
│   │   │   │   │               │   │   ├── Example.java
│   │   │   │   │               │   │   ├── GrammarTopic.java
│   │   │   │   │               │   │   ├── Section.java
│   │   │   │   │               │   │   └── SubSection.java
│   │   │   │   │               │   ├── PracticeTopic.java
│   │   │   │   │               │   ├── PracticeTopic.java~
│   │   │   │   │               │   ├── QuizQuestion.java
│   │   │   │   │               │   └── QuizQuestion.java~
│   │   │   │   │               ├── network
│   │   │   │   │               │   ├── ApiClient.java
│   │   │   │   │               │   └── ApiResponseListener.java
│   │   │   │   │               ├── utils
│   │   │   │   │               │   ├── SpacedRepetitionScheduler.java
│   │   │   │   │               │   ├── TranslateUtils.java
│   │   │   │   │               │   ├── TranslateUtils.java~
│   │   │   │   │               │   └── Utils.java
│   │   │   │   │               ├── viewmodel
│   │   │   │   │               │   └── VocabularyViewModel.java
│   │   │   │   │               └── MainApplication.java
│   │   │   │   ├── res
│   │   │   │   │   ├── animator
│   │   │   │   │   │   ├── flashcard_flip_back_in.xml
│   │   │   │   │   │   └── flashcard_flip_front_out.xml
│   │   │   │   │   ├── drawable
│   │   │   │   │   │   ├── background_example_block.xml
│   │   │   │   │   │   ├── ic_book_24.xml
│   │   │   │   │   │   ├── ic_brightness_6_24.xml
│   │   │   │   │   │   ├── ic_delete_24.xml
│   │   │   │   │   │   ├── ic_grammar_24.xml
│   │   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   │   ├── ic_next.xml
│   │   │   │   │   │   ├── ic_previous.xml
│   │   │   │   │   │   ├── quiz_option_background.xml
│   │   │   │   │   │   ├── quiz_option_background_correct.xml
│   │   │   │   │   │   ├── quiz_option_background_default.xml
│   │   │   │   │   │   ├── quiz_option_background_disabled.xml
│   │   │   │   │   │   ├── quiz_option_background_incorrect.xml
│   │   │   │   │   │   └── quiz_option_background_selector.xml
│   │   │   │   │   ├── layout
│   │   │   │   │   │   ├── activity_flashcard.xml
│   │   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   │   ├── activity_story.xml
│   │   │   │   │   │   ├── activity_vocabulary_list.xml
│   │   │   │   │   │   ├── dialog_add_words.xml
│   │   │   │   │   │   ├── dialog_add_words.xml~
│   │   │   │   │   │   ├── dialog_definition_layout.xml
│   │   │   │   │   │   ├── dialog_definition_layout.xml~
│   │   │   │   │   │   ├── dialog_set_level.xml
│   │   │   │   │   │   ├── fragment_grammar.xml
│   │   │   │   │   │   ├── fragment_grammar_detail.xml
│   │   │   │   │   │   ├── fragment_grammar_exercise.xml
│   │   │   │   │   │   ├── fragment_grammar_exercise.xml~
│   │   │   │   │   │   ├── fragment_grammar_exercise_quiz.xml
│   │   │   │   │   │   ├── fragment_grammar_exercise_quiz.xml~
│   │   │   │   │   │   ├── fragment_grammar_practice_list.xml
│   │   │   │   │   │   ├── fragment_grammar_result.xml
│   │   │   │   │   │   ├── fragment_grammar_theory_list.xml
│   │   │   │   │   │   ├── fragment_practice.xml
│   │   │   │   │   │   ├── fragment_practice.xml~
│   │   │   │   │   │   ├── fragment_toeic_part5.xml
│   │   │   │   │   │   ├── fragment_toeic_part5_quiz.xml
│   │   │   │   │   │   ├── fragment_toeic_part5_quiz.xml~
│   │   │   │   │   │   ├── fragment_vocabulary.xml
│   │   │   │   │   │   ├── item_flashcard.xml
│   │   │   │   │   │   ├── item_grammar_example_block.xml
│   │   │   │   │   │   ├── item_grammar_list.xml
│   │   │   │   │   │   ├── item_grammar_paragraph.xml
│   │   │   │   │   │   ├── item_grammar_section_header.xml
│   │   │   │   │   │   ├── item_grammar_single_example.xml
│   │   │   │   │   │   ├── item_grammar_subsection_header.xml
│   │   │   │   │   │   ├── item_grammar_topic.xml
│   │   │   │   │   │   ├── item_practice_header.xml
│   │   │   │   │   │   ├── item_practice_topic.xml
│   │   │   │   │   │   └── item_word.xml
│   │   │   │   │   ├── menu
│   │   │   │   │   │   ├── bottom_nav_menu.xml
│   │   │   │   │   │   └── main_options_menu.xml
│   │   │   │   │   ├── mipmap-anydpi-v26
│   │   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   │   ├── mipmap-hdpi
│   │   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   │   ├── mipmap-mdpi
│   │   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   │   ├── mipmap-xhdpi
│   │   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   │   ├── mipmap-xxhdpi
│   │   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   │   ├── mipmap-xxxhdpi
│   │   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   │   ├── navigation
│   │   │   │   │   │   └── mobile_navigation.xml
│   │   │   │   │   ├── values
│   │   │   │   │   │   ├── colors.xml
│   │   │   │   │   │   ├── plurals.xml
│   │   │   │   │   │   ├── strings.xml
│   │   │   │   │   │   ├── styles.xml
│   │   │   │   │   │   └── themes.xml
│   │   │   │   │   ├── values-night
│   │   │   │   │   │   ├── colors.xml
│   │   │   │   │   │   ├── styles.xml
│   │   │   │   │   │   └── themes.xml
│   │   │   │   │   └── xml
│   │   │   │   │       ├── backup_rules.xml
│   │   │   │   │       └── data_extraction_rules.xml
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   │               └── example
│   │   │                   └── learning_english
│   │   │                       └── ExampleUnitTest.java
│   │   ├── .gitignore
│   │   ├── build.gradle
│   │   └── proguard-rules.pro
│   ├── gradle
│   │   └── libs.versions.toml
│   ├── .gitignore
│   ├── build.gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── local.properties
│   └── settings.gradle
```
