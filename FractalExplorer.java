import java.awt.*;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;



public class FractalExplorer {
    public static void main(String[] args) {
        FractalExplorer explorer = new FractalExplorer(600); // инициализация изображения
        explorer.createAndShowGUI(); // отображение интерфейса
        explorer.drawFractal(); // рисовка фрактала
    }
    // размер экрана
    private int dSize;
    // ссылка для обновления отображения в разных методах в процессе вычисления фрактала
    private JImageDisplay img;
    // ссылка на базовый класс для отображения других фракталов в будущем
    private FractalGenerator gener;
    // диапозон комплексной плоскости, которая выводится на экран
    private Rectangle2D.Double rect;
    // поля для фрейма
    private JButton resD;
    private  JButton saveB;
    // Combo-boxe
    private JComboBox<String> chooseFractal;
    // оставшиеся строки для отрисовки фрактала
    private int rowsRemaining;


    // конструктор отображения
    public FractalExplorer (int displaySize) {
        // сохраняем размер отображения
        dSize = displaySize;
        // инициализируем объекты диапозона
        gener = new Mandelbrot();
        // инициализируем объекты генератора
        rect = new Rectangle2D.Double();
        gener.getInitialRange(rect);
    }
    // метод для отображения результата
    public void createAndShowGUI () {
        img = new JImageDisplay(dSize, dSize);
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Fractal Generator"); // даем заголовок нашему окну
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // закрытие окна по умолчанию
        ResetButtom res = new ResetButtom(); // добавление функции обработки кнопки

        // создание панельки для отображения фрактала
        JPanel fractalPanel = new JPanel();
        fractalPanel.setLayout(new BorderLayout());
        // создаем панельку с названием фрактала и добавляем ее на основную панель
        JLabel choF = new JLabel("Fractal:");
        fractalPanel.add(choF);
        // добавляем названия наших фракталов для выбора
        chooseFractal = new JComboBox<String>();
        chooseFractal.addItem(Mandelbrot.nameF());
        chooseFractal.addItem(Tricorn.nameF());
        chooseFractal.addItem(BurningShip.nameF());
        chooseFractal.addActionListener(res);
        fractalPanel.add(chooseFractal, BorderLayout.NORTH);
        // панель для кнопок сохранения и сброса
        JPanel buttom = new JPanel();
        buttom.setLayout(new BorderLayout());
        // кнопка для сброса
        resD = new JButton("Reset Display");
        resD.setActionCommand("reset");
        resD.addActionListener(res);
        // кнопка сохранения
        saveB = new JButton("Save Image");
        saveB.setActionCommand("save");
        saveB.addActionListener(res);
        // добавляем кнопкки на дополнительную панель
        buttom.add(resD, BorderLayout.WEST);
        buttom.add(saveB, BorderLayout.EAST);
        // устанавливаем область и кнопки сброса и сохранения на места
        fractalPanel.add(img, BorderLayout.CENTER);
        fractalPanel.add(buttom, BorderLayout.SOUTH);

        frame.getContentPane().add(fractalPanel);
        frame.addMouseListener(new MouseHandler()); // обновление функции мыши
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
    // геттеры для координат фрактала - упрощение обращения к главному геттеру
    private double getXCoord (int x) {
        return FractalGenerator.getCoord(rect.x, rect.x + rect.width, dSize, x);
    }
    private double getYCoord (int y) {
        return FractalGenerator.getCoord(rect.y, rect.y + rect.height, dSize, y);
    }


    // рисовка фрактала
    private void drawFractal () {
        this.enableUI(false);
        rowsRemaining = dSize;
        for(int i = 0; i < dSize; i++) {
            // создаем отдельный рабочий объект
            FractalWorker worker = new FractalWorker(i);
            // запускаем задачу в фоновом режиме
            worker.execute();
        }
        img.repaint();
    }
    // обработка кнопок
    private class ResetButtom implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String action = event.getActionCommand();
            if ("reset".equals(action)) { // кнопка сброса фрактала
                rect = new Rectangle2D.Double();
                // возвращаемся к начальному диапазону
                gener.getInitialRange(rect);
                // рисуем фрактал заново
                drawFractal();
            } else if (event.getSource() == chooseFractal) {
                // обработка кнопки выбора фрактала
                String selectedF = chooseFractal.getSelectedItem().toString();
                if (selectedF.equals(Mandelbrot.nameF())) {
                    gener = new Mandelbrot();
                } else if (selectedF.equals(Tricorn.nameF())) {
                    gener = new Tricorn();
                } else if (selectedF.equals(BurningShip.nameF())) {
                    gener = new BurningShip();
                } else {
                    JOptionPane.showMessageDialog(null, "Error: chooseFractal unknown choice. Try again");
                    return;
                }
            rect = new Rectangle2D.Double();
            gener.getInitialRange(rect);
            drawFractal();
            } else if ("save".equals(action)) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);
                // если пользователь не отменил операцию - обрабатываем сохранение файла
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = chooser.getSelectedFile();
                        String filePath = file.getPath();
                        if (!filePath.toLowerCase().endsWith(".png")) {
                            file = new File(filePath + ".png");
                        }
                        ImageIO.write(img.getImg(), "png", file); // сохранение файла на диск
                    } catch (IOException exception) {
                        JOptionPane.showMessageDialog(null, "Error: can not Save Image. Try again!");
                        exception.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error: unknown action");
            }

        }
    }
    // обработка работы мыши
    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            double xCoord = getXCoord(event.getX());
            double yCoord = getYCoord(event.getY());
            gener.recenterAndZoomRange(rect, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }
    // FractalWorker, отвечающий за вычисление значений цвета для одной строки фрактала
    private class FractalWorker extends SwingWorker<Object, Object> {
        private int yCord;
        private int[] rgbArray;
        // сохранение координаты y
        public FractalWorker(int y) {
            yCord = y;
        }
        //
        protected Object doInBackground() {
            rgbArray = new int[dSize];
            // drawFractal в новом месте
            double xCoord;
            double yCoord = getYCoord(yCord);
            float numIters;
            float hue;
            int rgbColor = 0; // цвет по умолчанию - черный
            // обработка пикселей
            for (int x = 0; x < dSize; x++) {
                xCoord = getXCoord(x);
                numIters = gener.numIterations(xCoord, yCoord);
                if (numIters < 0) {
                    rgbColor = 0;
                } else {
                    hue = 0.7f + (float) numIters / 200f;
                    rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                }
                rgbArray[x] = rgbColor;
                }
            return null;
            }
        protected void done() {
            // перебираем массив строк, рисуя пиксели
            for(int i = 0; i < dSize; i++) {
                img.drawPixel(i, yCord, rgbArray[i]);
            }
            img.repaint(0,0,yCord, dSize, 1); //перерисовываем строку
            if ((--rowsRemaining) < 1) { // проверка, есть ли еще строки
                enableUI(true);
            }
        }
    }
    // метод обновления кнопок
    private void enableUI(boolean val) {
        chooseFractal.setEnabled(val);
        saveB.setEnabled(val);
        resD.setEnabled(val);
    }
}