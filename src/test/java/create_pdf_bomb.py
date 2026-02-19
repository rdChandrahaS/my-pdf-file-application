# 1. Python script to generate a massive PDF (Stress Test)
# Install dependency first: pip install fpdf

from fpdf import FPDF

pdf = FPDF()
pdf.set_font("Arial", size=12)

# Adjust range for larger/smaller file size
for i in range(50000):
    pdf.add_page()
    pdf.multi_cell(0, 10, "STRESS TEST " * 500)

pdf.output("massive_stress_test.pdf")